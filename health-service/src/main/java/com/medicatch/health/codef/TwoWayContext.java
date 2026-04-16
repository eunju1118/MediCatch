package com.medicatch.health.codef;

import com.medicatch.health.dto.request.TwoWayRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;

/**
 * CODEF 2-Way 인증 메타데이터 — 불변 값 객체
 *
 * <p>1차 요청에서 CF-03002 응답 수신 시 data 필드에 포함되는 정보를 담는다.
 * 사용자 인증(카카오/PASS/SMS) 완료 후 2차 요청 시 파라미터로 재사용된다.</p>
 *
 * <pre>
 * // 1차 응답에서 컨텍스트 추출
 * TwoWayContext ctx = twoWayAuthHandler.extractContext(firstResponse);
 *
 * // 2차 요청 파라미터에 주입
 * twoWayAuthHandler.injectTwoWayParams(params, ctx);
 * codefClient.requestCertification(url, params);
 * </pre>
 */
@Getter
@Builder
@ToString
public class TwoWayContext {

    /** 작업 인덱스 */
    private final int jobIndex;

    /** 스레드 인덱스 */
    private final int threadIndex;

    /** 트랜잭션 고유 식별자 */
    private final String jti;

    /** 1차 요청 타임스탬프 (epoch milliseconds) */
    private final long twoWayTimestamp;

    // ── 팩토리 메서드 ─────────────────────────────────────────────────────

    /**
     * 프론트엔드에서 전달된 {@link TwoWayRequest} DTO → TwoWayContext 변환
     */
    public static TwoWayContext from(TwoWayRequest request) {
        return TwoWayContext.builder()
                .jobIndex(request.getJobIndex())
                .threadIndex(request.getThreadIndex())
                .jti(request.getJti())
                .twoWayTimestamp(request.getTwoWayTimestamp())
                .build();
    }

    // ── 파라미터 변환 ─────────────────────────────────────────────────────

    /**
     * CODEF 2차 요청의 "twoWayInfo" 값으로 쓸 파라미터 맵 생성
     *
     * <pre>
     * {
     *   "jobIndex":        0,
     *   "threadIndex":     0,
     *   "jti":             "트랜잭션ID",
     *   "twoWayTimestamp": 1234567890
     * }
     * </pre>
     */
    public HashMap<String, Object> toParamMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("jobIndex", jobIndex);
        map.put("threadIndex", threadIndex);
        map.put("jti", jti);
        map.put("twoWayTimestamp", twoWayTimestamp);
        return map;
    }
}
