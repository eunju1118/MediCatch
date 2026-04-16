package com.medicatch.insurance.controller;

import com.medicatch.insurance.dto.request.InsuranceContractRequest;
import com.medicatch.insurance.dto.request.InsuranceRegisterRequest;
import com.medicatch.insurance.dto.response.ApiResponse;
import com.medicatch.insurance.service.InsuranceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/insurance")
@RequiredArgsConstructor
public class InsuranceController {

    private final InsuranceService insuranceService;

    /**
     * POST /api/insurance/insurance/register
     * 내보험다보여 회원가입
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(
            @Valid @RequestBody InsuranceRegisterRequest request) {
        Map<String, Object> result = insuranceService.register(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * POST /api/insurance/insurance/contracts
     * 보험 계약정보 조회
     */
    @PostMapping("/contracts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getContracts(
            @Valid @RequestBody InsuranceContractRequest request) {
        Map<String, Object> result = insuranceService.getContractInfo(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * POST /api/insurance/insurance/contracts/certify
     * 보험 계약정보 2차 인증
     */
    @PostMapping("/contracts/certify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> certifyContracts(
            @Valid @RequestBody InsuranceContractRequest original,
            @RequestParam Integer jobIndex,
            @RequestParam Integer threadIndex,
            @RequestParam String jti,
            @RequestParam Long twoWayTimestamp) {
        Map<String, Object> result = insuranceService.certifyContractInfo(
                original, jobIndex, threadIndex, jti, twoWayTimestamp);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
