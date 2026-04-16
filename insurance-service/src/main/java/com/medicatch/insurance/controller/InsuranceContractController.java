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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 내보험다보여 계약정보 조회 컨트롤러
 *
 * <p>API Gateway 경유 실제 경로: {@code /api/insurance/contract[/certify]}</p>
 *
 * <h3>2-Way 인증 시퀀스</h3>
 * <pre>
 * [프론트] POST /api/insurance/contract
 *   ↓ { id, password, identity, userName, phoneNo, ... }
 * [서버]  1차 요청 → CODEF
 *   ↓ CF-03002: { twoWayRequired: true, twoWayContext: { jti, ... } }
 * [사용자] 간편인증 수행
 *   ↓
 * [프론트] POST /api/insurance/contract/certify
 *   ↓ { original: { ...1차와 동일... }, jti, jobIndex, ... }
 * [서버]  2차 요청 → CODEF
 *   ↓ CF-00000: { twoWayRequired: false, contractInfo: { ... } }
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/contract")
@RequiredArgsConstructor
public class InsuranceContractController {

    private final InsuranceContractService contractService;

    /**
     * 내보험다보여 계약정보 조회 1차 요청
     *
     * <p><b>Gateway 경로:</b> {@code POST /api/insurance/contract}</p>
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InsuranceContractResult>> requestContract(
            @Valid @RequestBody InsuranceContractRequest request) {

        log.debug("계약정보 조회 1차 요청 수신: user={}, type={}", request.getUserName(), request.getType());
        InsuranceContractResult result = contractService.requestContract(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 내보험다보여 계약정보 2차 인증 — 간편인증 완료 후 호출
     *
     * <p><b>Gateway 경로:</b> {@code POST /api/insurance/contract/certify}</p>
     */
    @PostMapping("/certify")
    public ResponseEntity<ApiResponse<InsuranceContractResult>> certify(
            @Valid @RequestBody InsuranceContractCertifyRequest request) {

        log.debug("계약정보 2차 인증 수신: jti={}", request.getJti());
        InsuranceContractResult result = contractService.certifyAndFetch(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
