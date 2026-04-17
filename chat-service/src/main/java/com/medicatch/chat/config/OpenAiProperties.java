package com.medicatch.chat.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "openai")
@Getter
@Setter
public class OpenAiProperties {

    private String apiKey;
    private String model       = "gpt-4o";
    private int    maxTokens   = 1500;
    private double temperature = 0.7;
    /** OpenAI API 응답 대기 타임아웃 (초) */
    private int    timeoutSeconds = 60;
}
