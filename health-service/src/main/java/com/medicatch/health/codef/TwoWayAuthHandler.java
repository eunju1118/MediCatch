package com.medicatch.health.codef;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * CODEF 2-Way 인증 공통 플로우 처리기
 *
 * <p>모든 CODEF API가 따르는 2단계 인증 흐름을 캡슐화한다.</p>
 *
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  [1단계] executeFirst(url, params)                              │
 * │     └─ 1차 요청 → CODEF 서버                                    │
 * │          ├─ 성공(CF-00000)   : response.isSuccess() == true     │
 * │          └─ 인증 필요(CF-03002): response.isTwoWayRequired()    │
 * │               └─ extractContext(response) → TwoWayContext       │
 * │                    └─ 사용자: 카카오/PASS/SMS 인증 수행          │
 * │                                                                  │
 * │  [2단계] executeSecond(url, params, ctx)                        │
 * │     └─ injectTwoWayParams(params, ctx)                          │
 * │          → is2Way:true, simpleAuth:"1", twoWayInfo:{...}        │
 * │     └─ 2차 요청 → 최종 데이터 수신                              │
 * └─────────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p>사용 예시 (MedicalService):</p>
 * <pre>
 * // 1차 요청
 * CodefResponse first = handler.executeFirst(PRODUCT_URL, params);
 * if (first.isTwoWayRequired()) {
 *     TwoWayContext ctx = handler.extractContext(first);
 *     // ctx를 세션 또는 응답에 담아 프론트엔드로 전달
 *     return;
 * }
 *
 * // 2차 요청 (사용자 인증 완료 후)
 * CodefResponse final = handler.executeSecond(PRODUCT_URL, params, ctx);
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TwoWayAuthHandler {

    private final CodefClient codefClient;
    private final CodefResponseParser responseParser;

    // ── 1단계: 1차 요청 ───────────────────────────────────────────────────

    /**
     * 1차 요청 실행 후 파싱된 {@link CodefResponse} 반환
     *
     * <ul>
     *   <li>CF-00000: 1회 요청으로 완료 — {@code response.isSuccess()} 확인</li>
     *   <li>CF-03002: 간편인증 대기 — {@code response.isTwoWayRequired()} 확인 후
     *       {@link #extractContext(CodefResponse)}로 컨텍스트 추출</li>
     * </ul>
     *
     * @param productUrl CODEF 상품 URL (예: /v1/kr/public/hw/hira-list/my-medical-information)
     * @param params     API 요청 파라미터 (민감 정보는 RSA 암호화 후 전달)
     */
    public CodefResponse executeFirst(String productUrl, HashMap<String, Object> params) {
        log.info("[2-Way] 1차 요청 시작: url={}", productUrl);

        String raw = codefClient.requestProduct(productUrl, params);
        CodefResponse response = responseParser.parse(raw);

        if (response.isTwoWayRequired()) {
            String jti = (String) response.dataAsMap().get("jti");
            log.info("[2-Way] CF-03002 수신 — 간편인증 대기: url={}, jti={}", productUrl, jti);
        } else if (response.isSuccess()) {
            log.info("[2-Way] 1차 요청 성공 (2-Way 불필요): url={}", productUrl);
        } else {
            log.warn("[2-Way] 1차 요청 오류: url={}, code={}, message={}",
                    productUrl, response.getResultCode(), response.getResultMessage());
        }

        return response;
    }

    // ── 2단계: 2차 인증 요청 ──────────────────────────────────────────────

    /**
     * 2차 인증 요청 실행
     *
     * <p>{@code params}에 {@link #injectTwoWayParams(HashMap, TwoWayContext)}를 적용한 후
     * {@link CodefClient#requestCertification}을 호출한다.</p>
     *
     * @param productUrl 1차 요청과 동일한 CODEF 상품 URL
     * @param params     1차 요청과 동일한 파라미터 맵 (is2Way 등이 이 메서드에서 주입됨)
     * @param ctx        1차 응답에서 추출한 {@link TwoWayContext}
     */
    public CodefResponse executeSecond(String productUrl,
                                       HashMap<String, Object> params,
                                       TwoWayContext ctx) {
        log.info("[2-Way] 2차 인증 요청: url={}, jti={}", productUrl, ctx.getJti());

        injectTwoWayParams(params, ctx);
        String raw = codefClient.requestCertification(productUrl, params);
        CodefResponse response = responseParser.parse(raw);

        if (response.isSuccess()) {
            log.info("[2-Way] 인증 성공: url={}", productUrl);
        } else {
            log.warn("[2-Way] 2차 요청 오류: url={}, code={}, message={}",
                    productUrl, response.getResultCode(), response.getResultMessage());
        }

        return response;
    }

    // ── 컨텍스트 추출 ─────────────────────────────────────────────────────

    /**
     * CF-03002 응답에서 {@link TwoWayContext} 추출
     *
     * @throws CodefApiException CF-03002가 아닌 응답이거나 jti가 없는 경우
     */
    public TwoWayContext extractContext(CodefResponse response) {
        return responseParser.extractTwoWayContext(response);
    }

    // ── 파라미터 주입 ─────────────────────────────────────────────────────

    /**
     * 2차 요청 파라미터 맵에 2-Way 인증 필드를 직접 주입 (in-place 수정)
     *
     * <p>주입되는 필드:</p>
     * <pre>
     * params.put("is2Way",    true);
     * params.put("simpleAuth", "1");
     * params.put("twoWayInfo", {
     *     "jobIndex":        ctx.getJobIndex(),
     *     "threadIndex":     ctx.getThreadIndex(),
     *     "jti":             ctx.getJti(),
     *     "twoWayTimestamp": ctx.getTwoWayTimestamp()
     * });
     * </pre>
     *
     * @param params 기존 1차 요청 파라미터 맵 (수정됨)
     * @param ctx    1차 응답에서 추출한 TwoWayContext
     */
    public void injectTwoWayParams(HashMap<String, Object> params, TwoWayContext ctx) {
        params.put("is2Way", true);
        params.put("simpleAuth", "1");
        params.put("twoWayInfo", ctx.toParamMap());
        log.debug("[2-Way] 파라미터 주입 완료: jti={}, twoWayTimestamp={}",
                ctx.getJti(), ctx.getTwoWayTimestamp());
    }
}
