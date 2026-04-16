package com.medicatch.insurance.codef;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class CodefResponse {

    private final String resultCode;
    private final String resultMessage;
    private final Object rawData;
    private final String rawJson;

    public boolean isSuccess() {
        return "CF-00000".equals(resultCode);
    }

    public boolean isTwoWayRequired() {
        return "CF-03002".equals(resultCode);
    }

    public boolean isError() {
        return !isSuccess() && !isTwoWayRequired();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> dataAsMap() {
        if (rawData instanceof Map) {
            return (Map<String, Object>) rawData;
        }
        throw new IllegalStateException("응답 data가 Map이 아님: " + (rawData != null ? rawData.getClass() : "null"));
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> dataAsList() {
        if (rawData instanceof List) {
            return (List<Map<String, Object>>) rawData;
        }
        throw new IllegalStateException("응답 data가 List가 아님: " + (rawData != null ? rawData.getClass() : "null"));
    }

    public boolean hasData() {
        return rawData != null;
    }
}
