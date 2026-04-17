package com.medicatch.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicatch.chat.client.HealthServiceClient;
import com.medicatch.chat.config.OpenAiProperties;
import com.medicatch.chat.dto.ChatRequest;
import com.medicatch.chat.dto.ChatResponse;
import com.medicatch.chat.dto.ConversationMessage;
import com.medicatch.chat.openai.OpenAiStreamChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * GPT-4o Chat Completions API 연동 서비스.
 *
 * <h3>플로우</h3>
 * <pre>
 * 1. Feign → health-service에서 userId별 진료기록 + 건강검진 데이터 조회
 * 2. 데이터를 시스템 프롬프트 컨텍스트로 주입
 * 3. 대화 이력(conversationHistory) 포함 메시지 목록 구성
 * 4a. chat()      : OpenAI 동기 호출 → ChatResponse 반환
 * 4b. streamToEmitter(): OpenAI stream=true → SSE Emitter에 청크 전송
 * </pre>
 */
@Slf4j
@Service
public class GptService {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            당신은 사용자의 건강 데이터를 기반으로 맞춤 건강 상담을 제공하는 AI 어시스턴트입니다.

            [사용자 진료 기록]
            %s

            [최근 건강검진 결과]
            %s

            중요 지침:
            - 의학적 진단이나 처방을 제공하지 않습니다.
            - 증상이 심각한 경우 반드시 전문의 상담을 권장합니다.
            - 건강 데이터가 없는 경우에도 일반적인 건강 정보를 친절하게 안내합니다.
            - 한국어로 명확하고 공감 어린 어조로 응답합니다.
            """;

    private final OpenAiProperties openAiProperties;
    private final HealthServiceClient healthServiceClient;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public GptService(OpenAiProperties openAiProperties,
                      HealthServiceClient healthServiceClient,
                      ObjectMapper objectMapper) {
        this.openAiProperties   = openAiProperties;
        this.healthServiceClient = healthServiceClient;
        this.objectMapper        = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(OPENAI_API_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiProperties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // ── 동기 호출 (POST /message) ─────────────────────────────────────────

    /**
     * GPT-4o에 메시지를 전송하고 완성된 응답을 동기로 반환한다.
     */
    @SuppressWarnings("unchecked")
    public ChatResponse chat(ChatRequest request) {
        String systemPrompt = buildSystemPrompt(request.getUserId());
        List<Map<String, String>> messages = buildMessages(systemPrompt, request);

        Map<String, Object> body = buildRequestBody(messages, false);
        log.debug("GPT 요청: userId={}, historySize={}", request.getUserId(),
                request.getConversationHistory() == null ? 0 : request.getConversationHistory().size());

        Map<String, Object> response = webClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return extractChatResponse(response);
    }

    // ── SSE 스트리밍 (GET /stream) ────────────────────────────────────────

    /**
     * GPT-4o stream=true 응답을 청크 단위로 SseEmitter에 전송한다.
     *
     * <p>OpenAI가 {@code data: [DONE]} 을 보내면 emitter를 완료 처리한다.</p>
     */
    public void streamToEmitter(ChatRequest request, SseEmitter emitter) {
        String systemPrompt = buildSystemPrompt(request.getUserId());
        List<Map<String, String>> messages = buildMessages(systemPrompt, request);
        Map<String, Object> body = buildRequestBody(messages, true);

        log.debug("GPT 스트리밍 시작: userId={}", request.getUserId());

        webClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> !line.isBlank())
                .filter(line -> line.startsWith("data:"))
                .map(line -> line.substring(5).trim())
                .takeWhile(data -> !"[DONE]".equals(data))
                .mapNotNull(this::parseChunkContent)
                .filter(content -> !content.isEmpty())
                .subscribe(
                        content -> sendToEmitter(emitter, content),
                        error   -> {
                            log.error("GPT 스트리밍 오류: {}", error.getMessage());
                            emitter.completeWithError(error);
                        },
                        () -> {
                            log.debug("GPT 스트리밍 완료");
                            emitter.complete();
                        }
                );
    }

    // ── 내부 유틸 ─────────────────────────────────────────────────────────

    /**
     * health-service에서 유저 건강 데이터를 조회해 시스템 프롬프트를 구성한다.
     * 데이터 조회 실패 시 "조회된 데이터 없음"으로 대체해 채팅을 계속 허용한다.
     */
    private String buildSystemPrompt(String userId) {
        String medicalContext = "조회된 진료기록 없음";
        String checkupContext = "조회된 건강검진 결과 없음";

        if (userId != null && !userId.isBlank()) {
            try {
                Map<String, Object> summary = healthServiceClient.getHealthData(userId);

                Object medicalData = summary.get("medicalData");
                Object checkupData = summary.get("checkupData");

                if (medicalData != null) {
                    medicalContext = objectMapper.writeValueAsString(medicalData);
                }
                if (checkupData != null) {
                    checkupContext = objectMapper.writeValueAsString(checkupData);
                }
                log.debug("건강 데이터 조회 성공: userId={}, hasMedical={}, hasCheckup={}",
                        userId, summary.get("hasMedicalData"), summary.get("hasCheckupData"));

            } catch (Exception e) {
                log.warn("건강 데이터 조회 실패 (채팅 계속): userId={}, error={}", userId, e.getMessage());
            }
        }

        return String.format(SYSTEM_PROMPT_TEMPLATE, medicalContext, checkupContext);
    }

    private List<Map<String, String>> buildMessages(String systemPrompt, ChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        if (request.getConversationHistory() != null) {
            for (ConversationMessage msg : request.getConversationHistory()) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
        }

        messages.add(Map.of("role", "user", "content", request.getMessage()));
        return messages;
    }

    private Map<String, Object> buildRequestBody(List<Map<String, String>> messages, boolean stream) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model",       openAiProperties.getModel());
        body.put("messages",    messages);
        body.put("max_tokens",  openAiProperties.getMaxTokens());
        body.put("temperature", openAiProperties.getTemperature());
        if (stream) {
            body.put("stream", true);
        }
        return body;
    }

    @SuppressWarnings("unchecked")
    private ChatResponse extractChatResponse(Map<String, Object> response) {
        if (response == null) {
            return new ChatResponse("응답을 받지 못했습니다.", LocalDateTime.now());
        }
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, String> message = (Map<String, String>) choices.get(0).get("message");
            return new ChatResponse(message.get("content"), LocalDateTime.now());
        } catch (Exception e) {
            log.error("GPT 응답 파싱 실패: {}", e.getMessage());
            return new ChatResponse("응답 처리 중 오류가 발생했습니다.", LocalDateTime.now());
        }
    }

    private String parseChunkContent(String json) {
        try {
            OpenAiStreamChunk chunk = objectMapper.readValue(json, OpenAiStreamChunk.class);
            return chunk.contentDelta();
        } catch (Exception e) {
            return "";
        }
    }

    private void sendToEmitter(SseEmitter emitter, String content) {
        try {
            emitter.send(SseEmitter.event().data(content));
        } catch (IOException e) {
            log.warn("SSE 전송 실패: {}", e.getMessage());
            emitter.completeWithError(e);
        }
    }
}
