package com.medicatch.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /** HS256 서명 키 (최소 256 bit) */
    private String secret;

    /** 토큰 유효시간 (초) */
    private long expiration = 86400;
}
