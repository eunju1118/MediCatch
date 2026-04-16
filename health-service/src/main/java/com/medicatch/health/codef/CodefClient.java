package com.medicatch.health.codef;

import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import io.codef.api.EasyCodefUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * CODEF API 공통 클라이언트
 * - 1차 요청(requestProduct) 및 2차 인증(requestCertification) 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodefClient {

    private final EasyCodef easyCodef;
    private final EasyCodefServiceType serviceType;

    @Value("${codef.public-key}")
    private String publicKey;

    /**
     * 1차 요청 (CODEF API 호출)
     */
    public String requestProduct(String productUrl, HashMap<String, Object> params) {
        try {
            return easyCodef.requestProduct(productUrl, serviceType, params);
        } catch (Exception e) {
            log.error("CODEF 1차 요청 실패: url={}, error={}", productUrl, e.getMessage());
            throw new CodefApiException("CODEF API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 2차 인증 요청 (2-Way Authentication)
     */
    public String requestCertification(String productUrl, HashMap<String, Object> params) {
        try {
            return easyCodef.requestCertification(productUrl, serviceType, params);
        } catch (Exception e) {
            log.error("CODEF 2차 인증 실패: url={}, error={}", productUrl, e.getMessage());
            throw new CodefApiException("CODEF 2차 인증 실패: " + e.getMessage(), e);
        }
    }

    /**
     * RSA 암호화 (비밀번호, 주민번호 뒷자리 등)
     */
    public String encryptRSA(String plainText) {
        try {
            return EasyCodefUtil.encryptRSA(plainText, publicKey);
        } catch (Exception e) {
            log.error("RSA 암호화 실패: {}", e.getMessage());
            throw new CodefApiException("RSA 암호화 실패", e);
        }
    }
}
