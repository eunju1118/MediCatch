package com.medicatch.insurance.codef;

import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import io.codef.api.EasyCodefUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CodefInsuranceClient {

    private static final Set<String> SENSITIVE_KEYS =
            Set.of("identity", "password", "certFile", "keyFile", "certPassword");

    private final EasyCodef easyCodef;
    private final EasyCodefServiceType serviceType;
    private final CodefInsuranceProperties codefProperties;

    public String requestProduct(String productUrl, HashMap<String, Object> params) {
        try {
            log.debug("CODEF 1차 요청: url={}, params={}", productUrl, sanitize(params));
            return easyCodef.requestProduct(productUrl, serviceType, params);
        } catch (Exception e) {
            log.error("CODEF 1차 요청 실패: {}", e.getMessage());
            throw new CodefInsuranceException("CODEF API 호출 실패: " + e.getMessage(), e);
        }
    }

    public String requestCertification(String productUrl, HashMap<String, Object> params) {
        try {
            log.debug("CODEF 2차 요청: url={}, params={}", productUrl, sanitize(params));
            return easyCodef.requestCertification(productUrl, serviceType, params);
        } catch (Exception e) {
            log.error("CODEF 2차 인증 실패: {}", e.getMessage());
            throw new CodefInsuranceException("CODEF 2차 인증 실패: " + e.getMessage(), e);
        }
    }

    public String encryptRSA(String plainText) {
        try {
            return EasyCodefUtil.encryptRSA(plainText, codefProperties.getPublicKey());
        } catch (Exception e) {
            throw new CodefInsuranceException("RSA 암호화 실패", e);
        }
    }

    private Map<String, Object> sanitize(HashMap<String, Object> params) {
        return params.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> SENSITIVE_KEYS.contains(e.getKey()) ? "***" : e.getValue()
                ));
    }
}
