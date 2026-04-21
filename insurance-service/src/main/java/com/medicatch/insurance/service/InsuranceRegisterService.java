package com.medicatch.insurance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicatch.insurance.codef.*;
import com.medicatch.insurance.dto.request.InsuranceRegisterCertifyRequest;
import com.medicatch.insurance.dto.request.InsuranceRegisterRequest;
import com.medicatch.insurance.dto.response.register.InsuranceRegisterResponse;
import com.medicatch.insurance.dto.response.register.InsuranceRegisterResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 내보험다보여 회원가입 신청 서비스
 *
 * CODEF API: /v1/kr/insurance/0001/credit4u/register
 *
 * <h3>플로우</h3>
 * <pre>
 * [1단계] POST /register
 *   └─ requestRegister(InsuranceRegisterRequest)
 *        ├─ CF-00000 : InsuranceRegisterResult.success(registerInfo) 반환
 *        └─ CF-03002 : InsuranceRegisterResult.pending(ctx)          반환
 *
 * [2단계] POST /register/certify
 *   └─ certifyAndComplete(InsuranceRegisterCertifyRequest)
 *        └─ CF-00000 : InsuranceRegisterResult.success(registerInfo) 반환
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceRegisterService {

    private static final String PRODUCT_URL = "/v1/kr/insurance/0001/credit4u/register";

    private final CodefInsuranceClient codefClient;
    private final TwoWayAuthHandler twoWayAuthHandler;
    private final ObjectMapper objectMapper;

    public InsuranceRegisterResult requestRegister(InsuranceRegisterRequest request) {
        log.info("내보험다보여 회원가입 신청: user={}", request.getUserName());

        CodefResponse response = twoWayAuthHandler.executeFirst(
                PRODUCT_URL, buildParams(request));

        if (response.isTwoWayRequired()) {
            TwoWayContext ctx = twoWayAuthHandler.extractContext(response);
            log.info("회원가입 신청 — 간편인증 대기: jti={}", ctx.getJti());
            return InsuranceRegisterResult.pending(ctx, response.getResultCode(), response.getResultMessage());
        }

        if (response.isError()) {
            log.error("회원가입 신청 오류: code={}, msg={}",
                    response.getResultCode(), response.getResultMessage());
            throw CodefInsuranceException.fromResponse(response, PRODUCT_URL);
        }

        return InsuranceRegisterResult.success(toRegisterResponse(response));
    }

    public InsuranceRegisterResult certifyAndComplete(InsuranceRegisterCertifyRequest certifyRequest) {
        log.info("회원가입 2차 인증: jti={}", certifyRequest.getJti());

        TwoWayContext ctx = TwoWayContext.builder()
                .jobIndex(certifyRequest.getJobIndex())
                .threadIndex(certifyRequest.getThreadIndex())
                .jti(certifyRequest.getJti())
                .twoWayTimestamp(certifyRequest.getTwoWayTimestamp())
                .build();

        CodefResponse response = twoWayAuthHandler.executeSecond(
                PRODUCT_URL, buildParams(certifyRequest.getOriginal()), ctx);

        if (response.isError()) {
            log.error("회원가입 2차 인증 오류: code={}, msg={}",
                    response.getResultCode(), response.getResultMessage());
            throw CodefInsuranceException.fromResponse(response, PRODUCT_URL);
        }

        return InsuranceRegisterResult.success(toRegisterResponse(response));
    }

    private HashMap<String, Object> buildParams(InsuranceRegisterRequest req) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("organization", "0001");
        params.put("userName",    req.getUserName());
        params.put("identity",    req.getIdentity());
        params.put("birthDate",   req.getBirthDate());
        params.put("telecom",     req.getTelecom());
        params.put("phoneNo",     req.getPhoneNo());
        params.put("authMethod",  req.getAuthMethod());
        params.put("type",        req.getType());
        params.put("id",          req.getId());
        params.put("password",    codefClient.encryptRSA(req.getPassword()));
        params.put("email",       req.getEmail());
        return params;
    }

    @SuppressWarnings("unchecked")
    private InsuranceRegisterResponse toRegisterResponse(CodefResponse response) {
        try {
            List<Map<String, Object>> dataList = response.dataAsList();
            if (dataList == null || dataList.isEmpty()) {
                log.warn("회원가입 응답 data 배열이 비어있음");
                return new InsuranceRegisterResponse();
            }
            return objectMapper.convertValue(dataList.get(0), InsuranceRegisterResponse.class);
        } catch (Exception e) {
            log.error("회원가입 응답 파싱 실패: {}", e.getMessage());
            throw new CodefInsuranceException("회원가입 응답 변환 실패: " + e.getMessage(), e);
        }
    }
}
