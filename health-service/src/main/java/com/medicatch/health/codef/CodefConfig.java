package com.medicatch.health.codef;

import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CODEF EasyCodef 초기화 설정
 *
 * <p>application.yml의 {@code codef.mode} 값에 따라 DEMO/REAL 모드를 선택한다.</p>
 * <pre>
 * codef:
 *   client-id:     ${CODEF_CLIENT_ID}
 *   client-secret: ${CODEF_CLIENT_SECRET}
 *   public-key:    ${CODEF_PUBLIC_KEY}
 *   mode:          DEMO   # DEMO | REAL
 * </pre>
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(CodefProperties.class)
public class CodefConfig {

    @Bean
    public EasyCodef easyCodef(CodefProperties props) {
        EasyCodef codef = new EasyCodef();

        if ("API".equalsIgnoreCase(props.getMode())) {
            log.info("[CODEF] 실서비스 모드로 초기화");
            codef.setClientInfo(props.getClientId(), props.getClientSecret());
        } else {
            log.info("[CODEF] 데모 모드로 초기화 (codef.mode=DEMO)");
            codef.setClientInfoForDemo(props.getClientId(), props.getClientSecret());
        }

        codef.setPublicKey(props.getPublicKey());
        return codef;
    }

    @Bean
    public EasyCodefServiceType serviceType(CodefProperties props) {
        return "API".equalsIgnoreCase(props.getMode())
                ? EasyCodefServiceType.API
                : EasyCodefServiceType.DEMO;
    }
}
