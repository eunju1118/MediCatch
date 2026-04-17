package com.medicatch.health.controller;

import com.medicatch.health.dto.request.CheckupCertifyRequest;
import com.medicatch.health.dto.request.CheckupRequest;
import com.medicatch.health.dto.response.ApiResponse;
import com.medicatch.health.service.CheckupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/checkup")
@RequiredArgsConstructor
public class CheckupController {

    private final CheckupService checkupService;

    /**
     * POST /api/health/checkup/request — 건강검진결과 조회 1차 요청
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Map<String, Object>>> requestCheckup(
            @Valid @RequestBody CheckupRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        Map<String, Object> result = checkupService.requestCheckup(request, userId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * POST /api/health/checkup/certify — 건강검진결과 조회 2차 인증
     */
    @PostMapping("/certify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> certify(
            @Valid @RequestBody CheckupCertifyRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        Map<String, Object> result = checkupService.certifyAndFetch(request, userId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
