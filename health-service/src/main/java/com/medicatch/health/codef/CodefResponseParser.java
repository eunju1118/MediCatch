package com.medicatch.health.codef;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * CODEF API 응답 JSON 파서
 *
 * <p>원본 JSON 문자열을 {@link CodefResponse}로 변환하고,
 * CF-03002 응답에서 {@link TwoWayContext}를 추출하는 역할을 담당한다.</p>
 *
 * <p>CODEF 응답 구조:</p>
 * <pre>
 * {
 *   "result": {
 *     "code":    "CF-00000",
 *     "message": "성공"
 *   },
 *   "data": [ { ... } ]          // 성공: 배열
 * }
 *
 * {
 *   "result": {
 *     "code":    "CF-03002",
 *     "message": "추가 인증이 필요합니다"
 *   },
 *   "data": {                    // 2-Way: 객체
 *     "jobIndex":        0,
 *     "threadIndex":     0,
 *     "jti":             "트랜잭션ID",
 *     "twoWayTimestamp": 1234567890
 *   }
 * }
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodefResponseParser {

    private final ObjectMapper objectMapper;

    // ── 파싱 ──────────────────────────────────────────────────────────────

    /**
     * 원본 JSON 문자열 → {@link CodefResponse} 파싱
     * JSON 파싱 오류 시 resultCode = "PARSE_ERROR" 인 응답을 반환하며 예외를 던지지 않는다.
     */
    @SuppressWarnings("unchecked")
    public CodefResponse parse(String rawJson) {
        try {
            Map<String, Object> root = objectMapper.readValue(rawJson, Map.class);

            // result 블록에서 코드·메시지 추출
            Map<String, Object> result = (Map<String, Object>) root.get("result");
            String code    = extractString(result, "code",    CodefResultCode.UNKNOWN.getCode());
            String message = extractString(result, "message", "");

            Object data = root.get("data");

            log.debug("CODEF 응답 파싱 완료: code={}, message={}, dataType={}",
                    code, message,
                    data != null ? data.getClass().getSimpleName() : "null");

            return CodefResponse.builder()
                    .resultCode(code)
                    .resultMessage(message)
                    .rawData(data)
                    .rawJson(rawJson)
                    .build();

        } catch (Exception e) {
            log.error("CODEF 응답 JSON 파싱 실패: error={}, raw={}", e.getMessage(), rawJson);
            return CodefResponse.builder()
                    .resultCode(CodefResultCode.PARSE_ERROR.getCode())
                    .resultMessage("응답 파싱 실패: " + e.getMessage())
                    .rawData(null)
                    .rawJson(rawJson)
                    .build();
        }
    }

    // ── 2-Way 컨텍스트 추출 ────────────────────────────────────────────────

    /**
     * CF-03002 응답의 data 블록에서 {@link TwoWayContext} 추출
     *
     * @param response {@link #parse(String)} 결과 — isTwoWayRequired() == true 이어야 함
     * @throws CodefApiException response가 CF-03002가 아니거나 jti 필드가 없는 경우
     */
    public TwoWayContext extractTwoWayContext(CodefResponse response) {
        validateTwoWayResponse(response);

        Map<String, Object> data = response.dataAsMap();

        String jti = (String) data.get("jti");
        if (jti == null || jti.isBlank()) {
            throw new CodefApiException(
                "2-Way 컨텍스트 파싱 실패: jti 필드 누락",
                response.getResultCode(), null
            );
        }

        TwoWayContext ctx = TwoWayContext.builder()
                .jobIndex(toInt(data.get("jobIndex")))
                .threadIndex(toInt(data.get("threadIndex")))
                .jti(jti)
                .twoWayTimestamp(toLong(data.get("twoWayTimestamp")))
                .build();

        log.debug("TwoWayContext 추출: {}", ctx);
        return ctx;
    }

    // ── 내부 유틸 ─────────────────────────────────────────────────────────

    private void validateTwoWayResponse(CodefResponse response) {
        if (!response.isTwoWayRequired()) {
            throw new CodefApiException(
                "2-Way 컨텍스트 추출 실패: 응답 코드가 CF-03002가 아닙니다 (actual=" +
                response.getResultCode() + ")",
                response.getResultCode(), null
            );
        }
    }

    private String extractString(Map<String, Object> map, String key, String defaultValue) {
        if (map == null) return defaultValue;
        Object val = map.get(key);
        return val instanceof String s ? s : defaultValue;
    }

    private int toInt(Object val) {
        return val instanceof Number n ? n.intValue() : 0;
    }

    private long toLong(Object val) {
        return val instanceof Number n ? n.longValue() : 0L;
    }
}
