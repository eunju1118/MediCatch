package com.medicatch.health.codef;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * CODEF API 응답 래퍼 — 원본 JSON을 파싱한 불변 값 객체
 *
 * <p>CODEF 응답 구조:</p>
 * <pre>
 * {
 *   "result": { "code": "CF-00000", "message": "성공" },
 *   "data": ...   // 성공: List / 2-Way 대기: Map
 * }
 * </pre>
 */
@Getter
@Builder
public class CodefResponse {

    /** CODEF 결과 코드 (예: CF-00000, CF-03002) */
    private final String resultCode;

    /** CODEF 결과 메시지 */
    private final String resultMessage;

    /**
     * CODEF "data" 필드 원본
     * - 성공 응답      : {@code List<Map<String, Object>>}
     * - CF-03002 응답  : {@code Map<String, Object>} (jobIndex, threadIndex, jti, twoWayTimestamp)
     */
    private final Object rawData;

    /** 원본 JSON 문자열 (디버깅용) */
    private final String rawJson;

    // ── 상태 판별 ─────────────────────────────────────────────────────────

    /** CF-00000 성공 여부 */
    public boolean isSuccess() {
        return CodefResultCode.SUCCESS.getCode().equals(resultCode);
    }

    /** CF-03002 — 간편인증 대기 상태 여부 */
    public boolean isTwoWayRequired() {
        return CodefResultCode.TWO_WAY_REQUIRED.getCode().equals(resultCode);
    }

    /** 성공도 2-Way 대기도 아닌 오류 상태 */
    public boolean isError() {
        return !isSuccess() && !isTwoWayRequired();
    }

    // ── 데이터 접근 ───────────────────────────────────────────────────────

    /**
     * data 필드를 {@code Map<String, Object>}로 반환
     * CF-03002 응답(jobIndex, threadIndex, jti, twoWayTimestamp 포함) 접근 시 사용
     *
     * @throws IllegalStateException data가 Map이 아닌 경우
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> dataAsMap() {
        if (rawData instanceof Map) {
            return (Map<String, Object>) rawData;
        }
        throw new IllegalStateException(
            "data 필드가 Map이 아닙니다. 실제 타입: " +
            (rawData != null ? rawData.getClass().getSimpleName() : "null")
        );
    }

    /**
     * data 필드를 {@code List<Map<String, Object>>}로 반환
     * 건강검진·진료기록 등 성공 응답 접근 시 사용
     *
     * @throws IllegalStateException data가 List가 아닌 경우
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> dataAsList() {
        if (rawData instanceof List) {
            return (List<Map<String, Object>>) rawData;
        }
        throw new IllegalStateException(
            "data 필드가 List가 아닙니다. 실제 타입: " +
            (rawData != null ? rawData.getClass().getSimpleName() : "null")
        );
    }

    /** rawData가 null이거나 비어있는지 확인 */
    public boolean hasData() {
        if (rawData == null) return false;
        if (rawData instanceof List<?> l) return !l.isEmpty();
        if (rawData instanceof Map<?, ?> m) return !m.isEmpty();
        return true;
    }
}
