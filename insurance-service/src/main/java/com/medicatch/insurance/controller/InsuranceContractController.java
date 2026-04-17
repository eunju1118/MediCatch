package com.medicatch.insurance.controller;

import com.medicatch.insurance.dto.request.InsuranceContractCertifyRequest;
import com.medicatch.insurance.dto.request.InsuranceContractRequest;
import com.medicatch.insurance.dto.response.ApiResponse;
import com.medicatch.insurance.dto.response.contract.InsuranceContractResult;
import com.medicatch.insurance.service.InsuranceContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/contract")
@RequiredArgsConstructor
public class InsuranceContractController {

    private final InsuranceContractService contractService;

    /** POST /api/insurance/contract — 계약정보 조회 1차 요청 */
    @PostMapping
    public ResponseEntity<ApiResponse<InsuranceContractResult>> requestContract(
            @Valid @RequestBody InsuranceContractRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.debug("계약정보 조회 1차 요청 수신: user={}, type={}, userId={}", request.getUserName(), request.getType(), userId);
        InsuranceContractResult result = contractService.requestContract(request, userId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /** POST /api/insurance/contract/certify — 2차 인증 */
    @PostMapping("/certify")
    public ResponseEntity<ApiResponse<InsuranceContractResult>> certify(
            @Valid @RequestBody InsuranceContractCertifyRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.debug("계약정보 2차 인증 수신: jti={}, userId={}", request.getJti(), userId);
        InsuranceContractResult result = contractService.certifyAndFetch(request, userId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
