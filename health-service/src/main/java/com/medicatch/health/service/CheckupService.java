package com.medicatch.health.service;

import com.medicatch.health.codef.CodefClient;
import com.medicatch.health.codef.CodefResponse;
import com.medicatch.health.codef.TwoWayAuthHandler;
import com.medicatch.health.codef.TwoWayContext;
import com.medicatch.health.dto.request.CheckupRequest;
import com.medicatch.health.dto.request.TwoWayRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 건강검진결과 조회 서비스
 * CODEF API: /v1/kr/health/0002/nhis/health-checkup (organization: 0002)
 *
 * <p>출력 필드:</p>
 * <ul>
 *   <li>resCheckupTarget  — 검진대상 이름</li>
 *   <li>resResultList     — 전체 검진결과 리스트</li>
 *   <li>resPreviewList    — 검진 요약 (키·몸무게·혈압·혈당 등)</li>
 *   <li>resReferenceList  — 검진 소견 리스트</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckupService {

    private static final String PRODUCT_URL =
            "/v1/kr/health/0002/nhis/health-checkup";

    private final CodefClient codefClient;
    private final TwoWayAuthHandler twoWayAuthHandler;

    // ── 1차 요청 ──────────────────────────────────────────────────────────

    /**
     * 건강검진결과 조회 1차 요청
     *
     * <p>응답 맵의 {@code isTwoWayRequired} 값이 {@code true}이면
     * 프론트엔드가 간편인증 수행 후 {@link #certifyAndFetch}를 호출해야 한다.</p>
     */
    public Map<String, Object> requestCheckup(CheckupRequest request) {
        log.info("건강검진결과 조회 1차 요청: searchYear={}-{}",
                request.getSearchStartYear(), request.getSearchEndYear());
        CodefResponse response = twoWayAuthHandler.executeFirst(PRODUCT_URL, buildParams(request));
        return toResponseMap(response);
    }

    // ── 2차 인증 ──────────────────────────────────────────────────────────

    /**
     * 건강검진결과 조회 2차 인증 — 간편인증 완료 후 최종 데이터 수신
     *
     * @param twoWayRequest 1차 응답에서 전달받은 2-Way 메타데이터
     * @param original      1차 요청과 동일한 원본 요청 객체
     */
    public Map<String, Object> certifyAndFetch(TwoWayRequest twoWayRequest, CheckupRequest original) {
        log.info("건강검진결과 조회 2차 인증: jti={}", twoWayRequest.getJti());
        TwoWayContext ctx = TwoWayContext.from(twoWayRequest);
        CodefResponse response = twoWayAuthHandler.executeSecond(PRODUCT_URL, buildParams(original), ctx);
        return toResponseMap(response);
    }

    // ── 내부 유틸 ─────────────────────────────────────────────────────────

    private HashMap<String, Object> buildParams(CheckupRequest req) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("organization",     "0002");
        params.put("loginType",        req.getLoginType());
        params.put("loginTypeLevel",   req.getLoginTypeLevel());
        params.put("identity",         codefClient.encryptRSA(req.getIdentity()));
        params.put("birthDate",        req.getBirthDate());
        params.put("certType",         req.getCertType());
        params.put("inquiryType",      req.getInquiryType());
        params.put("searchStartYear",  req.getSearchStartYear());
        params.put("searchEndYear",    req.getSearchEndYear());
        params.put("type",             req.getType());
        return params;
    }

    private Map<String, Object> toResponseMap(CodefResponse response) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("resultCode",       response.getResultCode());
        map.put("resultMessage",    response.getResultMessage());
        map.put("isTwoWayRequired", response.isTwoWayRequired());
        map.put("data",             response.getRawData());

        if (response.isError()) {
            log.warn("건강검진결과 조회 오류 응답: code={}, message={}",
                    response.getResultCode(), response.getResultMessage());
        }
        return map;
    }
}
