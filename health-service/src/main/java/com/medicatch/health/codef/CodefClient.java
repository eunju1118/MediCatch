package com.medicatch.health.codef;

import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * CODEF API 저수준 클라이언트
 *
 * <p>EasyCodef SDK를 래핑하며 아래 3가지 역할만 담당한다.</p>
 * <ol>
 *   <li>1차 요청 — {@link #requestProduct}</li>
 *   <li>2차 인증 요청 — {@link #requestCertification}</li>
 *   <li>RSA 암호화 — {@link #encryptRSA}</li>
 * </ol>
 *
 * <p>2-Way 인증 플로우 조립은 {@link TwoWayAuthHandler}가,
 * 응답 JSON 파싱은 {@link CodefResponseParser}가 담당한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodefClient {

    /** 로그 출력 시 값을 마스킹할 파라미터 키 목록 */
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "identity", "password", "certFile", "keyFile", "certPassword"
    );

    private final EasyCodef easyCodef;
    private final EasyCodefServiceType serviceType;
    private final CodefProperties codefProperties;

    // ── API 호출 ──────────────────────────────────────────────────────────

    /**
     * 1차 요청 (requestProduct)
     *
     * @param productUrl CODEF 상품 URL
     * @param params     요청 파라미터 (민감 정보는 호출 전 RSA 암호화 필수)
     * @return CODEF 원본 JSON 응답 문자열
     * @throws CodefApiException EasyCodef SDK 호출 실패 시
     */
    public String requestProduct(String productUrl, HashMap<String, Object> params) {
        log.debug("[CODEF] requestProduct: url={}, params={}", productUrl, sanitize(params));
        try {
            String result = easyCodef.requestProduct(productUrl, serviceType, params);
            log.debug("[CODEF] requestProduct 응답: url={}, body={}", productUrl, result);
            return result;
        } catch (Exception e) {
            log.error("[CODEF] requestProduct 실패: url={}, error={}", productUrl, e.getMessage());
            throw new CodefApiException(
                    "CODEF API 호출 실패 [" + productUrl + "]: " + e.getMessage(),
                    CodefResultCode.UNKNOWN.getCode(), productUrl, e
            );
        }
    }

    /**
     * 2차 인증 요청 (requestCertification)
     *
     * <p>파라미터에 is2Way, simpleAuth, twoWayInfo 가 포함되어 있어야 한다.
     * {@link TwoWayAuthHandler#injectTwoWayParams}로 주입하는 것을 권장한다.</p>
     *
     * @param productUrl 1차 요청과 동일한 CODEF 상품 URL
     * @param params     is2Way/simpleAuth/twoWayInfo 가 주입된 파라미터 맵
     * @return CODEF 원본 JSON 응답 문자열
     * @throws CodefApiException EasyCodef SDK 호출 실패 시
     */
    public String requestCertification(String productUrl, HashMap<String, Object> params) {
        log.debug("[CODEF] requestCertification: url={}, params={}", productUrl, sanitize(params));
        try {
            String result = easyCodef.requestCertification(productUrl, serviceType, params);
            log.debug("[CODEF] requestCertification 응답: url={}, body={}", productUrl, result);
            return result;
        } catch (Exception e) {
            log.error("[CODEF] requestCertification 실패: url={}, error={}", productUrl, e.getMessage());
            throw new CodefApiException(
                    "CODEF 2차 인증 실패 [" + productUrl + "]: " + e.getMessage(),
                    CodefResultCode.UNKNOWN.getCode(), productUrl, e
            );
        }
    }

    // ── 암호화 ────────────────────────────────────────────────────────────

    /**
     * RSA 공개키 암호화
     *
     * <p>비밀번호, 주민번호 뒷자리 등 민감 정보를 CODEF에 전송하기 전에 반드시 적용해야 한다.
     * 공개키는 {@code codef.public-key} 설정 값을 사용한다.</p>
     *
     * @param plainText 평문 (예: 주민번호 13자리, 비밀번호)
     * @return RSA 암호화된 Base64 문자열
     * @throws CodefApiException 암호화 실패 시
     */
//    public String encryptRSA(String plainText) {
//        try {
//            return EasyCodefUtil.encryptRSA(plainText, codefProperties.getPublicKey());
//        } catch (Exception e) {
//            log.error("[CODEF] RSA 암호화 실패: {}", e.getMessage());
//            throw new CodefApiException(
//                    "RSA 암호화 실패: " + e.getMessage(),
//                    CodefResultCode.UNKNOWN.getCode(), null, e
//            );
//        }
//    }
    public String encryptRSA(String plainText) {
        try {
            // 1. 공개키 디코딩 (Standard Base64 사용)
            byte[] bytePublicKey = Base64.getDecoder().decode(codefProperties.getPublicKey());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey key = keyFactory.generatePublic(new X509EncodedKeySpec(bytePublicKey));

            // 2. Cipher 설정 (RSA/ECB/PKCS1Padding)
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            // 3. 암호화 수행
            byte[] bytePlain = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] byteEncrypted = cipher.doFinal(bytePlain);

            // 4. 표준 Base64 인코딩 (여기서 URL-safe 문자 '_' 문제를 해결함)
            return Base64.getEncoder().encodeToString(byteEncrypted);

        } catch (Exception e) {
            log.error("[CODEF] 자체 RSA 암호화 실패: {}", e.getMessage());
            throw new CodefApiException(
                    "RSA 암호화 실패: " + e.getMessage(),
                    CodefResultCode.UNKNOWN.getCode(), null, e
            );
        }
    }

    // ── 내부 유틸 ─────────────────────────────────────────────────────────

    /** 민감 파라미터 값을 "***" 로 대체한 복사본 반환 (로그 출력 전용) */
    private Map<String, Object> sanitize(HashMap<String, Object> params) {
        Map<String, Object> safe = new HashMap<>(params);
        SENSITIVE_KEYS.forEach(key -> {
            if (safe.containsKey(key)) safe.put(key, "***");
        });
        return safe;
    }
}
