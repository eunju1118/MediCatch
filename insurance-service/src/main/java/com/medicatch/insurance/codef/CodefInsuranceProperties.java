package com.medicatch.insurance.codef;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "codef")
@Getter
@Setter
public class CodefInsuranceProperties {

    private String clientId;
    private String clientSecret;
    private String publicKey;
    private String mode = "DEMO";
}
