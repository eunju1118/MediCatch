package com.medicatch.health.controller;

import com.medicatch.health.dto.request.MedicalRequest;
import com.medicatch.health.dto.request.TwoWayRequest;
import com.medicatch.health.dto.response.ApiResponse;
import com.medicatch.health.service.MedicalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/medical")
@RequiredArgsConstructor
public class MedicalController {

    private final MedicalService medicalService;

    /**
     * POST /api/health/medical/request
     * 진료기록 조회 1차 요청
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Map<String, Object>>> requestMedical(
            @Valid @RequestBody MedicalRequest request) {
        Map<String, Object> result = medicalService.requestMedicalInfo(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * POST /api/health/medical/certify
     * 진료기록 조회 2차 인증 (간편인증 완료 후 호출)
     */
    @PostMapping("/certify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> certify(
            @Valid @RequestBody TwoWayRequest twoWayRequest,
            @RequestBody(required = false) MedicalRequest original) {
        Map<String, Object> result = medicalService.certifyAndFetch(twoWayRequest, original);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
