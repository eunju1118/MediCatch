package com.medicatch.health.codef;

import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodefConfig {

    @Value("${codef.client-id}")
    private String clientId;

    @Value("${codef.client-secret}")
    private String clientSecret;

    @Value("${codef.public-key}")
    private String publicKey;

    @Bean
    public EasyCodef easyCodef() {
        EasyCodef codef = new EasyCodef();
        codef.setClientInfoForDemo(clientId, clientSecret);
        codef.setPublicKey(publicKey);
        return codef;
    }

    @Bean
    public EasyCodefServiceType serviceType() {
        return EasyCodefServiceType.DEMO;
    }
}
