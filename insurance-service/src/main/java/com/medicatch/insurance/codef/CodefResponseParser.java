package com.medicatch.insurance.codef;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CodefResponseParser {

    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    public CodefResponse parse(String rawJson) {
        try {
            Map<String, Object> root = objectMapper.readValue(rawJson, Map.class);

            Map<String, Object> result = (Map<String, Object>) root.get("result");
            String code    = result != null ? (String) result.get("code")    : "PARSE_ERROR";
            String message = result != null ? (String) result.get("message") : "결과 코드 파싱 실패";

            Object data = root.get("data");

            return CodefResponse.builder()
                    .resultCode(code)
                    .resultMessage(message)
                    .rawData(data)
                    .rawJson(rawJson)
                    .build();

        } catch (Exception e) {
            log.error("CODEF 응답 파싱 실패: {}", e.getMessage());
            return CodefResponse.builder()
                    .resultCode("PARSE_ERROR")
                    .resultMessage("응답 JSON 파싱 실패: " + e.getMessage())
                    .rawJson(rawJson)
                    .build();
        }
    }

    public TwoWayContext extractTwoWayContext(CodefResponse response) {
        if (!response.isTwoWayRequired()) {
            throw new CodefInsuranceException("CF-03002 응답이 아님: " + response.getResultCode(),
                    "CF-03002", null);
        }

        Map<String, Object> dataMap = response.dataAsMap();

        Number jobIndex       = (Number) dataMap.get("jobIndex");
        Number threadIndex    = (Number) dataMap.get("threadIndex");
        String jti            = (String) dataMap.get("jti");
        Number twoWayTs       = (Number) dataMap.get("twoWayTimestamp");

        if (jti == null || twoWayTs == null) {
            throw new CodefInsuranceException("CF-03002 응답에 twoWayInfo 필드 누락", "CF-03002", null);
        }

        return TwoWayContext.builder()
                .jobIndex(jobIndex != null ? jobIndex.intValue() : 0)
                .threadIndex(threadIndex != null ? threadIndex.intValue() : 0)
                .jti(jti)
                .twoWayTimestamp(twoWayTs.longValue())
                .build();
    }
}
