package com.medicatch.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ChatRequest {

    @NotBlank
    private String message;                         // 사용자 질문

    private List<Map<String, String>> history;      // 이전 대화 기록

    private Map<String, Object> healthContext;       // 건강검진/진료기록 컨텍스트 (선택)
}
