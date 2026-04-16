package com.medicatch.chat.service;

import com.medicatch.chat.dto.ChatRequest;
import com.medicatch.chat.dto.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI GPT-4o Chat Completions API 연동 서비스
 */
@Slf4j
@Service
public class GptService {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o";
    private static final String SYSTEM_PROMPT =
            "당신은 헬스케어 전문 AI 어시스턴트입니다. " +
            "사용자의 건강검진 결과, 진료기록, 보험 정보를 바탕으로 " +
            "개인화된 건강 조언을 제공합니다. " +
            "의학적 진단은 제공하지 않으며, 전문의 상담을 권장합니다.";

    private final WebClient webClient;

    public GptService(@Value("${openai.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(OPENAI_API_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * GPT-4o에 메시지 전송 및 응답 수신
     */
    @SuppressWarnings("unchecked")
    public ChatResponse chat(ChatRequest request) {
        List<Map<String, String>> messages = buildMessages(request);

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("messages", messages);
        body.put("max_tokens", 1000);
        body.put("temperature", 0.7);

        Map<String, Object> response = webClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return extractResponse(response);
    }

    private List<Map<String, String>> buildMessages(ChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 시스템 프롬프트
        String systemContent = SYSTEM_PROMPT;
        if (request.getHealthContext() != null && !request.getHealthContext().isEmpty()) {
            systemContent += "\n\n[사용자 건강 데이터]\n" + request.getHealthContext().toString();
        }
        messages.add(Map.of("role", "system", "content", systemContent));

        // 이전 대화 기록
        if (request.getHistory() != null) {
            messages.addAll(request.getHistory());
        }

        // 현재 사용자 메시지
        messages.add(Map.of("role", "user", "content", request.getMessage()));

        return messages;
    }

    @SuppressWarnings("unchecked")
    private ChatResponse extractResponse(Map<String, Object> response) {
        if (response == null) {
            return new ChatResponse("응답을 받지 못했습니다.", MODEL, 0, 0);
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> choice = choices.get(0);
        Map<String, String> message = (Map<String, String>) choice.get("message");
        String reply = message.get("content");

        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        int promptTokens = (int) usage.get("prompt_tokens");
        int completionTokens = (int) usage.get("completion_tokens");

        return new ChatResponse(reply, MODEL, promptTokens, completionTokens);
    }
}
