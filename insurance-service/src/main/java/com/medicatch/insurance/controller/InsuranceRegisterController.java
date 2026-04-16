package com.medicatch.insurance.controller;

import com.medicatch.insurance.dto.request.InsuranceRegisterCertifyRequest;
import com.medicatch.insurance.dto.request.InsuranceRegisterRequest;
import com.medicatch.insurance.dto.response.ApiResponse;
import com.medicatch.insurance.dto.response.register.InsuranceRegisterResult;
import com.medicatch.insurance.service.InsuranceRegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 내보험다보여 회원가입 신청 컨트롤러
 *
 * <p>API Gateway 경유 실제 경로: {@code /api/insurance/register[/certify]}</p>
 *
 * <h3>2-Way 인증 시퀀스</h3>
 * <pre>
 * [프론트] POST /api/insurance/register
 *   ↓ { userName, identity, birthDate, phoneNo, ... }
 * [서버]  1차 요청 → CODEF
 *   ↓ CF-03002: { twoWayRequired: true, twoWayContext: { jti, ... } }
 * [사용자] 간편인증 수행
 *   ↓
 * [프론트] POST /api/insurance/register/certify
 *   ↓ { original: { ...1차와 동일... }, jti, jobIndex, ... }
 * [서버]  2차 요청 → CODEF
 *   ↓ CF-00000: { twoWayRequired: false, registerInfo: { ... } }
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class InsuranceRegisterController {

    private final InsuranceRegisterService registerService;

    /**
     * 내보험다보여 회원가입 신청 1차 요청
     *
     * <p><b>Gateway 경로:</b> {@code POST /api/insurance/register}</p>
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InsuranceRegisterResult>> requestRegister(
            @Valid @RequestBody InsuranceRegisterRequest request) {

        log.debug("회원가입 신청 1차 요청 수신: user={}", request.getUserName());
        InsuranceRegisterResult result = registerService.requestRegister(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 내보험다보여 회원가입 2차 인증 — 간편인증 완료 후 호출
     *
     * <p><b>Gateway 경로:</b> {@code POST /api/insurance/register/certify}</p>
     */
    @PostMapping("/certify")
    public ResponseEntity<ApiResponse<InsuranceRegisterResult>> certify(
            @Valid @RequestBody InsuranceRegisterCertifyRequest request) {

        log.debug("회원가입 2차 인증 수신: jti={}", request.getJti());
        InsuranceRegisterResult result = registerService.certifyAndComplete(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
