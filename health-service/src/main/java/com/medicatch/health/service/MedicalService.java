package com.medicatch.health.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicatch.health.codef.CodefClient;
import com.medicatch.health.dto.request.MedicalRequest;
import com.medicatch.health.dto.request.TwoWayRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 진료기록 조회 서비스
 * CODEF API: /v1/kr/public/hw/hira-list/my-medical-information
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalService {

    private static final String PRODUCT_URL = "/v1/kr/public/hw/hira-list/my-medical-information";

    private final CodefClient codefClient;
    private final ObjectMapper objectMapper;

    /**
     * 1차 요청 — 진료기록 조회 시작
     * 응답 코드 CF-03002: 2-Way 인증 필요
     */
    public Map<String, Object> requestMedicalInfo(MedicalRequest request) {
        HashMap<String, Object> params = buildParams(request);
        String result = codefClient.requestProduct(PRODUCT_URL, params);
        return parseResult(result);
    }

    /**
     * 2차 요청 — 간편인증(카카오/PASS/SMS) 완료 후 최종 데이터 수신
     */
    public Map<String, Object> certifyAndFetch(TwoWayRequest twoWayRequest, MedicalRequest original) {
        HashMap<String, Object> params = buildParams(original);

        HashMap<String, Object> twoWayInfo = new HashMap<>();
        twoWayInfo.put("jobIndex", twoWayRequest.getJobIndex());
        twoWayInfo.put("threadIndex", twoWayRequest.getThreadIndex());
        twoWayInfo.put("jti", twoWayRequest.getJti());
        twoWayInfo.put("twoWayTimestamp", twoWayRequest.getTwoWayTimestamp());

        params.put("is2Way", true);
        params.put("simpleAuth", "1");
        params.put("twoWayInfo", twoWayInfo);

        String result = codefClient.requestCertification(PRODUCT_URL, params);
        return parseResult(result);
    }

    private HashMap<String, Object> buildParams(MedicalRequest req) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("organization", "0020");
        params.put("loginType", req.getLoginType());
        params.put("loginTypeLevel", req.getLoginTypeLevel());
        params.put("userName", req.getUserName());
        params.put("identity", codefClient.encryptRSA(req.getIdentity()));
        params.put("phoneNo", req.getPhoneNo());
        params.put("telecom", req.getTelecom());
        params.put("authMethod", req.getAuthMethod());
        params.put("startDate", req.getStartDate());
        params.put("endDate", req.getEndDate());
        params.put("type", req.getType());
        return params;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseResult(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("응답 파싱 실패: {}", e.getMessage());
            return Map.of("raw", json);
        }
    }
}
