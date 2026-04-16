package com.medicatch.chat.controller;

import com.medicatch.chat.dto.ChatRequest;
import com.medicatch.chat.dto.ChatResponse;
import com.medicatch.chat.service.GptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final GptService gptService;

    /**
     * POST /api/chat/chat/message
     * AI 건강 채팅 메시지 전송
     */
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = gptService.chat(request);
        return ResponseEntity.ok(response);
    }
}
