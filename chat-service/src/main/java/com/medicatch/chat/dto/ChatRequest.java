package com.medicatch.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatRequest {

    /** Gateway가 주입하는 X-User-Id (클라이언트에서 설정 불필요) */
    private String userId;

    @NotBlank
    private String message;

    /** 이전 대화 기록 (없으면 null 또는 빈 리스트) */
    private List<ConversationMessage> conversationHistory;
}
