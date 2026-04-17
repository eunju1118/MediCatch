package com.medicatch.chat.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OpenAI Chat Completions 스트리밍 응답 청크 DTO.
 *
 * <p>OpenAI의 {@code stream: true} 응답 각 줄을 역직렬화하는 데 사용한다.</p>
 * <pre>
 * data: {"choices":[{"delta":{"content":"Hello"},"index":0,"finish_reason":null}]}
 * </pre>
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiStreamChunk {

    private List<Choice> choices;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {

        private Delta delta;

        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Delta {

        private String role;
        private String content;
    }

    /** 스트림 청크에서 텍스트 델타 추출 (null 이면 빈 문자열) */
    public String contentDelta() {
        if (choices == null || choices.isEmpty()) return "";
        Delta delta = choices.get(0).getDelta();
        if (delta == null || delta.getContent() == null) return "";
        return delta.getContent();
    }
}
