package com.medicatch.insurance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicatch.insurance.codef.*;
import com.medicatch.insurance.dto.request.InsuranceContractCertifyRequest;
import com.medicatch.insurance.dto.request.InsuranceContractRequest;
import com.medicatch.insurance.dto.response.contract.InsuranceContractResponse;
import com.medicatch.insurance.dto.response.contract.InsuranceContractResult;
import com.medicatch.insurance.store.InsuranceDataStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
        log.info("내보험다보여 조회 시작: {}", request.getUserName());
        CodefResponse response = twoWayAuthHandler.executeFirst(PRODUCT_URL, buildParams(request));

        if (response.isTwoWayRequired()) {
            TwoWayContext ctx = twoWayAuthHandler.extractContext(response);
            return InsuranceContractResult.pending(ctx, response.getResultCode(), response.getResultMessage());
        }

        if (response.isError()) {
            throw CodefInsuranceException.fromResponse(response, PRODUCT_URL);
        }

        InsuranceContractResponse contractInfo = toContractResponse(response);
        insuranceDataStore.storeContractData(userId, contractInfo);
        return InsuranceContractResult.success(contractInfo);
    }

    public InsuranceContractResult certifyAndFetch(InsuranceContractCertifyRequest certifyRequest, String userId) {
        TwoWayContext ctx = TwoWayContext.builder()
                .jobIndex(certifyRequest.getJobIndex()).threadIndex(certifyRequest.getThreadIndex())
                .jti(certifyRequest.getJti()).twoWayTimestamp(certifyRequest.getTwoWayTimestamp()).build();

        CodefResponse response = twoWayAuthHandler.executeSecond(PRODUCT_URL, buildParams(certifyRequest.getOriginal()), ctx);

        if (response.isError()) {
            throw CodefInsuranceException.fromResponse(response, PRODUCT_URL);
        }

        InsuranceContractResponse contractInfo = toContractResponse(response);
        insuranceDataStore.storeContractData(userId, contractInfo);
        return InsuranceContractResult.success(contractInfo);
    }

    private HashMap<String, Object> buildParams(InsuranceContractRequest req) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("organization", "0001");
        params.put("id", req.getId());
        params.put("password", codefClient.encryptRSA(req.getPassword()));
        params.put("identity", req.getIdentity());
        params.put("type", req.getType());
        params.put("userName", req.getUserName());
        params.put("phoneNo", req.getPhoneNo());
        params.put("telecom", req.getTelecom());
        params.put("authMethod", req.getAuthMethod());
        return params;
    }

    @SuppressWarnings("unchecked")
    private InsuranceContractResponse toContractResponse(CodefResponse response) {
        try {
            if (!response.hasData()) {
                log.warn("CODEF 응답에 데이터 블록이 없습니다.");
                return new InsuranceContractResponse();
            }

            // 1. "data" 블록을 바로 가져옵니다. (테스트 코드의 responseData와 동일 계층)
            Map<String, Object> dataMap = response.dataAsMap();

            // 2. 만약 dataMap 안에 "contractInfo"라는 키로 실제 리스트들이 들어있는 경우와
            //    dataMap 자체가 리스트들을 가지고 있는 경우를 모두 대응합니다.
            Map<String, Object> targetMap;
            if (dataMap.containsKey("contractInfo") && dataMap.get("contractInfo") instanceof Map) {
                log.info("중첩 구조(contractInfo) 발견 - 하위 계층 파싱");
                targetMap = (Map<String, Object>) dataMap.get("contractInfo");
            } else {
                log.info("일반 구조 - data 블록 직접 파싱");
                targetMap = dataMap;
            }

            // 3. DTO로 변환
            InsuranceContractResponse result = objectMapper.convertValue(targetMap, InsuranceContractResponse.class);

            // 로그 출력 (파싱 성공 여부 확인)
            log.info("조회 결과 - 정액: {}건, 실손: {}건, 자동차: {}건",
                    count(result.getResFlatRateContractList()),
                    count(result.getResActualLossContractList()),
                    count(result.getResCarContractList()));

            return result;

        } catch (Exception e) {
            log.error("InsuranceContractResponse 변환 중 에러 발생: {}", e.getMessage());
            return new InsuranceContractResponse();
        }
    }

    private int count(java.util.List<?> list) {
        return list != null ? list.size() : 0;
    }
}