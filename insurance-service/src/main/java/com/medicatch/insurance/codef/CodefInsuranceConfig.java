package com.medicatch.insurance.codef;

import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(CodefInsuranceProperties.class)
public class CodefInsuranceConfig {

    @Bean
    public EasyCodef easyCodef(CodefInsuranceProperties props) {
        EasyCodef codef = new EasyCodef();
        if ("REAL".equalsIgnoreCase(props.getMode())) {
            codef.setClientInfo(props.getClientId(), props.getClientSecret());
            log.info("CODEF 실서비스 모드로 초기화");
        } else {
            codef.setClientInfoForDemo(props.getClientId(), props.getClientSecret());
            log.info("CODEF 데모 모드로 초기화");
        }
        codef.setPublicKey(props.getPublicKey());
        return codef;
    }

    @Bean
    public EasyCodefServiceType serviceType(CodefInsuranceProperties props) {
        return "REAL".equalsIgnoreCase(props.getMode())
                ? EasyCodefServiceType.REAL
                : EasyCodefServiceType.DEMO;
    }
}
