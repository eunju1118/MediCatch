package com.medicatch.insurance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicatch.insurance.codef.CodefInsuranceClient;
import com.medicatch.insurance.codef.CodefInsuranceException;
import com.medicatch.insurance.codef.CodefResponse;
import com.medicatch.insurance.codef.TwoWayAuthHandler;
import com.medicatch.insurance.codef.TwoWayContext;
import com.medicatch.insurance.dto.request.InsuranceContractCertifyRequest;
import com.medicatch.insurance.dto.request.InsuranceContractRequest;
import com.medicatch.insurance.dto.response.contract.InsuranceContractResponse;
import com.medicatch.insurance.dto.response.contract.InsuranceContractResult;
import com.medicatch.insurance.store.InsuranceDataStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceContractService {

    private static final String PRODUCT_URL = "/v1/kr/insurance/0001/credit4u/contract-info";

    private final CodefInsuranceClient codefClient;
    private final TwoWayAuthHandler twoWayAuthHandler;
    private final ObjectMapper objectMapper;
    private final InsuranceDataStore insuranceDataStore;

    public InsuranceContractResult requestContract(InsuranceContractRequest request, String userId) {
        log.info("내보험다보여 계약정보 조회: user={}, type={}", request.getUserName(), request.getType());

        CodefResponse response = twoWayAuthHandler.executeFirst(PRODUCT_URL, buildParams(request));

        if (response.isTwoWayRequired()) {
            TwoWayContext ctx = twoWayAuthHandler.extractContext(response);
            log.info("계약정보 조회 — 간편인증 대기: jti={}", ctx.getJti());
            return InsuranceContractResult.pending(ctx, response.getResultCode(), response.getResultMessage());
        }

        if (response.isError()) {
            log.error("계약정보 조회 오류: code={}, msg={}",
                    response.getResultCode(), response.getResultMessage());
            throw CodefInsuranceException.fromResponse(response, PRODUCT_URL);
        }

        InsuranceContractResponse contractInfo = toContractResponse(response);
        insuranceDataStore.storeContractData(userId, contractInfo);
        return InsuranceContractResult.success(contractInfo);
    }

    public InsuranceContractResult certifyAndFetch(InsuranceContractCertifyRequest certifyRequest, String userId) {
        log.info("계약정보 2차 인증: jti={}", certifyRequest.getJti());

        TwoWayContext ctx = TwoWayContext.builder()
                .jobIndex(certifyRequest.getJobIndex())
                .threadIndex(certifyRequest.getThreadIndex())
                .jti(certifyRequest.getJti())
                .twoWayTimestamp(certifyRequest.getTwoWayTimestamp())
                .build();

        CodefResponse response = twoWayAuthHandler.executeSecond(
                PRODUCT_URL, buildParams(certifyRequest.getOriginal()), ctx);

        if (response.isError()) {
            log.error("계약정보 2차 인증 오류: code={}, msg={}",
                    response.getResultCode(), response.getResultMessage());
            throw CodefInsuranceException.fromResponse(response, PRODUCT_URL);
        }

        InsuranceContractResponse contractInfo = toContractResponse(response);
        insuranceDataStore.storeContractData(userId, contractInfo);
        return InsuranceContractResult.success(contractInfo);
    }

    private HashMap<String, Object> buildParams(InsuranceContractRequest req) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("organization", "0001");
        params.put("id",          req.getId());
        params.put("password",    codefClient.encryptRSA(req.getPassword()));
        params.put("identity",    req.getIdentity());
        params.put("type",        req.getType());
        params.put("userName",    req.getUserName());
        params.put("phoneNo",     req.getPhoneNo());
        params.put("telecom",     req.getTelecom());
        params.put("authMethod",  req.getAuthMethod());
        return params;
    }

    @SuppressWarnings("unchecked")
    private InsuranceContractResponse toContractResponse(CodefResponse response) {
        try {
            if (!response.hasData()) {
                return new InsuranceContractResponse();
            }

            // 1. 전체 data Map을 가져옴
            Map<String, Object> dataMap = response.dataAsMap();

            // 2. dataMap 안에 있는 "contractInfo"를 꺼냄
            Object contractInfoObj = dataMap.get("contractInfo");

            if (contractInfoObj == null) {
                log.warn("응답에 contractInfo 필드가 없습니다.");
                return new InsuranceContractResponse();
            }

            // 3. contractInfo 객체를 DTO로 변환
            InsuranceContractResponse result = objectMapper.convertValue(contractInfoObj, InsuranceContractResponse.class);

            log.info("최종 파싱 완료: 정액보험 {}건",
                    result.getResFlatRateContractList() != null ? result.getResFlatRateContractList().size() : 0);

            return result;

        } catch (Exception e) {
            log.error("계약정보 변환 실패: {}", e.getMessage());
            throw new CodefInsuranceException("계약정보 변환 실패: " + e.getMessage(), e);
        }
    }
}
