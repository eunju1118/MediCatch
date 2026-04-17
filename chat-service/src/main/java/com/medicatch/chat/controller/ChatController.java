package com.medicatch.chat.controller;

import com.medicatch.chat.dto.ChatRequest;
import com.medicatch.chat.dto.ChatResponse;
import com.medicatch.chat.service.GptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 건강 채팅 컨트롤러.
 *
 * <p>API Gateway 경유 실제 경로 (StripPrefix=2로 /api/chat 제거):</p>
 * <ul>
 *   <li>{@code POST /api/chat/message} → {@code POST /message}</li>
 *   <li>{@code GET  /api/chat/stream}  → {@code GET  /stream}</li>
 * </ul>
 *
 * <p>Gateway가 JWT에서 추출한 {@code X-User-Id} 헤더를 ChatRequest에 주입한다.</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final GptService gptService;

    /**
     * GPT-4o 건강 채팅 — 동기 응답.
     *
     * <p><b>요청 예시:</b></p>
     * <pre>
     * POST /api/chat/message
     * {
     *   "message": "혈압이 높은데 어떻게 해야 하나요?",
     *   "conversationHistory": [
     *     {"role":"user",      "content":"안녕하세요"},
     *     {"role":"assistant", "content":"안녕하세요! 무엇을 도와드릴까요?"}
     *   ]
     * }
     * </pre>
     *
     * <p><b>응답 예시:</b></p>
     * <pre>
     * { "message": "...", "timestamp": "2024-03-15T10:30:00" }
     * </pre>
     */
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        request.setUserId(userId);
        log.debug("채팅 요청 수신: userId={}", userId);

        ChatResponse response = gptService.chat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GPT-4o 건강 채팅 — SSE 스트리밍 응답.
     *
     * <p>OpenAI {@code stream: true} 모드로 생성되는 토큰을 실시간으로 클라이언트에 전달한다.</p>
     *
     * <p><b>요청 예시:</b></p>
     * <pre>
     * GET /api/chat/stream?message=혈압이 높은데 어떻게 해야 하나요?
     * </pre>
     *
     * <p><b>SSE 이벤트 형식:</b></p>
     * <pre>
     * data: 혈압
     * data: 이 높은
     * data:  경우에는
     * ...
     * </pre>
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @RequestParam String message,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.debug("SSE 스트리밍 요청 수신: userId={}", userId);

        SseEmitter emitter = new SseEmitter(60_000L); // 60초 타임아웃

        ChatRequest request = new ChatRequest();
        request.setUserId(userId);
        request.setMessage(message);

        gptService.streamToEmitter(request, emitter);
        return emitter;
    }
}
