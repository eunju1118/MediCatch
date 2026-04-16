package com.medicatch.insurance.codef;

import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import io.codef.api.EasyCodefUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * 보험 서비스 CODEF API 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodefInsuranceClient {

    private final EasyCodef easyCodef;
    private final EasyCodefServiceType serviceType;

    @Value("${codef.public-key}")
    private String publicKey;

    public String requestProduct(String productUrl, HashMap<String, Object> params) {
        try {
            return easyCodef.requestProduct(productUrl, serviceType, params);
        } catch (Exception e) {
            log.error("CODEF 1차 요청 실패: {}", e.getMessage());
            throw new CodefInsuranceException("CODEF API 호출 실패: " + e.getMessage(), e);
        }
    }

    public String requestCertification(String productUrl, HashMap<String, Object> params) {
        try {
            return easyCodef.requestCertification(productUrl, serviceType, params);
        } catch (Exception e) {
            log.error("CODEF 2차 인증 실패: {}", e.getMessage());
            throw new CodefInsuranceException("CODEF 2차 인증 실패: " + e.getMessage(), e);
        }
    }

    public String encryptRSA(String plainText) {
        try {
            return EasyCodefUtil.encryptRSA(plainText, publicKey);
        } catch (Exception e) {
            throw new CodefInsuranceException("RSA 암호화 실패", e);
        }
    }
}
