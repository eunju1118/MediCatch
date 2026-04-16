package com.medicatch.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String reply;           // GPT 응답 텍스트
    private String model;           // 사용된 모델 (gpt-4o)
    private int promptTokens;
    private int completionTokens;
}
