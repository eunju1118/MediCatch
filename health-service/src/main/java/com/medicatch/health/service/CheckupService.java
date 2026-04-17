package com.medicatch.health.service;

import com.medicatch.health.codef.CodefClient;
import com.medicatch.health.codef.CodefResponse;
import com.medicatch.health.codef.TwoWayAuthHandler;
import com.medicatch.health.codef.TwoWayContext;
import com.medicatch.health.dto.request.CheckupCertifyRequest;
import com.medicatch.health.dto.request.CheckupRequest;
import com.medicatch.health.store.HealthDataStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 건강검진결과 조회 서비스
 * CODEF API: /v1/kr/health/0002/nhis/health-checkup (organization: 0002)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckupService {

    private static final String PRODUCT_URL = "/v1/kr/health/0002/nhis/health-checkup";

    private final CodefClient codefClient;
    private final TwoWayAuthHandler twoWayAuthHandler;
    private final HealthDataStore healthDataStore;

    public Map<String, Object> requestCheckup(CheckupRequest request, String userId) {
        log.info("건강검진결과 조회 1차 요청: searchYear={}-{}",
                request.getSearchStartYear(), request.getSearchEndYear());
        CodefResponse response = twoWayAuthHandler.executeFirst(PRODUCT_URL, buildParams(request));

        Map<String, Object> result = toResponseMap(response);
        if (response.isSuccess()) {
            healthDataStore.storeCheckupData(userId, result);
        }
        return result;
    }

    public Map<String, Object> certifyAndFetch(CheckupCertifyRequest certifyRequest, String userId) {
        log.info("건강검진결과 조회 2차 인증: jti={}", certifyRequest.getJti());

        TwoWayContext ctx = TwoWayContext.builder()
                .jobIndex(certifyRequest.getJobIndex())
                .threadIndex(certifyRequest.getThreadIndex())
                .jti(certifyRequest.getJti())
                .twoWayTimestamp(certifyRequest.getTwoWayTimestamp())
                .build();

        CodefResponse response = twoWayAuthHandler.executeSecond(
                PRODUCT_URL, buildParams(certifyRequest.getOriginal()), ctx);

        Map<String, Object> result = toResponseMap(response);
        if (response.isSuccess()) {
            healthDataStore.storeCheckupData(userId, result);
        }
        return result;
    }

    // ── 내부 유틸 ─────────────────────────────────────────────────────────

    private HashMap<String, Object> buildParams(CheckupRequest req) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("organization",    "0002");
        params.put("loginType",       req.getLoginType());
        params.put("loginTypeLevel",  req.getLoginTypeLevel());
        params.put("identity",        codefClient.encryptRSA(req.getIdentity()));
        params.put("birthDate",       req.getBirthDate());
        params.put("certType",        req.getCertType());
        params.put("inquiryType",     req.getInquiryType());
        params.put("searchStartYear", req.getSearchStartYear());
        params.put("searchEndYear",   req.getSearchEndYear());
        params.put("type",            req.getType());
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
