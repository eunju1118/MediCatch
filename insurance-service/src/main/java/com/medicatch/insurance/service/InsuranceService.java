package com.medicatch.insurance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicatch.insurance.codef.CodefInsuranceClient;
import com.medicatch.insurance.dto.request.InsuranceContractRequest;
import com.medicatch.insurance.dto.request.InsuranceRegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 내보험다보여 보험 정보 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceService {

    private static final String REGISTER_URL    = "/v1/kr/insurance/0001/credit4u/register";
    private static final String CONTRACT_URL    = "/v1/kr/insurance/0001/credit4u/contract-info";

    private final CodefInsuranceClient codefClient;
    private final ObjectMapper objectMapper;

    /**
     * 내보험다보여 회원가입 신청
     * 출력: resRegistrationStatus, resLoginId, resEmail
     */
    public Map<String, Object> register(InsuranceRegisterRequest request) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("organization", "0001");
        params.put("userName", request.getUserName());
        params.put("identity", codefClient.encryptRSA(request.getIdentity()));
        params.put("telecom", request.getTelecom());
        params.put("phoneNo", request.getPhoneNo());
        params.put("authMethod", request.getAuthMethod());
        params.put("type", request.getType());
        params.put("id", request.getId());
        params.put("password", codefClient.encryptRSA(request.getPassword()));
        params.put("email", request.getEmail());

        String result = codefClient.requestProduct(REGISTER_URL, params);
        return parseResult(result);
    }

    /**
     * 내보험다보여 계약정보 조회
     * 출력: resFlatRateContractList, resActualLossContractList,
     *       resCarContractList, resSavingsContractList 등
     */
    public Map<String, Object> getContractInfo(InsuranceContractRequest request) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("organization", "0001");
        params.put("id", request.getId());
        params.put("password", codefClient.encryptRSA(request.getPassword()));
        params.put("type", request.getType());
        params.put("userName", request.getUserName());
        params.put("phoneNo", request.getPhoneNo());
        params.put("telecom", request.getTelecom());
        params.put("authMethod", request.getAuthMethod());

        String result = codefClient.requestProduct(CONTRACT_URL, params);
        return parseResult(result);
    }

    /**
     * 계약정보 2차 인증
     */
    public Map<String, Object> certifyContractInfo(
            InsuranceContractRequest original,
            Integer jobIndex, Integer threadIndex,
            String jti, Long twoWayTimestamp) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("organization", "0001");
        params.put("id", original.getId());
        params.put("password", codefClient.encryptRSA(original.getPassword()));
        params.put("type", original.getType());
        params.put("userName", original.getUserName());
        params.put("phoneNo", original.getPhoneNo());
        params.put("telecom", original.getTelecom());
        params.put("authMethod", original.getAuthMethod());

        HashMap<String, Object> twoWayInfo = new HashMap<>();
        twoWayInfo.put("jobIndex", jobIndex);
        twoWayInfo.put("threadIndex", threadIndex);
        twoWayInfo.put("jti", jti);
        twoWayInfo.put("twoWayTimestamp", twoWayTimestamp);

        params.put("is2Way", true);
        params.put("simpleAuth", "1");
        params.put("twoWayInfo", twoWayInfo);

        String result = codefClient.requestCertification(CONTRACT_URL, params);
        return parseResult(result);
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
