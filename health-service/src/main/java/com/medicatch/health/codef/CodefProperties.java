package com.medicatch.health.codef;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CODEF 설정 프로퍼티
 * application.yml의 codef.* 값을 타입 안전하게 바인딩
 * codef:
 *   client-id:     ${CODEF_CLIENT_ID}
 *   client-secret: ${CODEF_CLIENT_SECRET}
 *   public-key:    ${CODEF_PUBLIC_KEY}
 *   mode:          DEMO   # DEMO | API
 */
@ConfigurationProperties(prefix = "codef")
@Getter
@Setter
public class CodefProperties {

    /** CODEF 클라이언트 ID */
    private String clientId;

    /** CODEF 클라이언트 시크릿 */
    private String clientSecret;

    /** CODEF RSA 공개키 — 민감 정보 암호화에 사용 */
    private String publicKey;

    /**
     * 서비스 모드
     * - DEMO : 데모 서버 (테스트용, 실제 조회 불가)
     * - API : 실서비스 서버
     */
    private String mode = "DEMO";
}
