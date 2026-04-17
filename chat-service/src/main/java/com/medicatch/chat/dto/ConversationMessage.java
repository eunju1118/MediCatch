package com.medicatch.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GPT 대화 메시지 단위 (role + content).
 *
 * <p>role: "user" | "assistant" | "system"</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMessage {

    private String role;
    private String content;
}
