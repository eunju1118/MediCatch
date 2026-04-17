package com.medicatch.health.controller;

import com.medicatch.health.dto.response.ApiResponse;
import com.medicatch.health.dto.response.report.HealthReportResponse;
import com.medicatch.health.service.HealthReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 건강 통합 리포트 컨트롤러.
 *
 * <p>API Gateway 경유 실제 경로:</p>
 * <ul>
 *   <li>{@code GET /api/health/report?userId={userId}&months=12}</li>
 * </ul>
 *
 * <p>Gateway가 JWT에서 추출한 {@code X-User-Id} 헤더를 우선 사용하며,
 * 쿼리 파라미터 {@code userId}로 대체할 수 있다.</p>
 *
 * <h3>응답 예시</h3>
 * <pre>
 * {
 *   "success": true, "message": "OK",
 *   "data": {
 *     "userId": "user123",
 *     "analyzedMonths": 12,
 *     "generatedAt": "2024-03-15T10:30:00",
 *     "totalVisitCount": 8,
 *     "totalMedicalCost": 320000,
 *     "totalPatientCost": 85000,
 *     "monthlyVisitCount": { "2024-01": 2, "2024-02": 1, ... },
 *     "departmentUsage":   { "내과": 4, "정형외과": 2, ... },
 *     "insuranceClaimable": [
 *       { "treatmentDate": "2024-03-10", "hospitalName": "서울병원",
 *         "department": "내과", "patientPayment": 15000,
 *         "claimableReason": "실손보험 적용 가능", "contractType": "실손" }
 *     ],
 *     "coverageGap": [
 *       { "department": "치과", "visitCount": 2, "totalPatientPayment": 80000,
 *         "gapType": "DENTAL", "recommendation": "치아보험 가입을 검토해보세요" }
 *     ],
 *     "insuranceDataAvailable": true
 *   }
 * }
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class HealthReportController {

    private final HealthReportService healthReportService;

    /**
     * 건강 통합 리포트 조회.
     *
     * @param userId      쿼리 파라미터 userId (선택 — X-User-Id 헤더가 우선)
     * @param months      분석 기간 개월 수 (기본 12)
     * @param headerUserId Gateway가 주입한 X-User-Id 헤더
     */
    @GetMapping
    public ResponseEntity<ApiResponse<HealthReportResponse>> getReport(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "12") int months,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {

        String effectiveUserId = (headerUserId != null && !headerUserId.isBlank())
                ? headerUserId : userId;

        if (effectiveUserId == null || effectiveUserId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("userId가 필요합니다 (X-User-Id 헤더 또는 userId 파라미터)"));
        }

        if (months < 1 || months > 60) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("months는 1~60 범위여야 합니다"));
        }

        log.debug("건강 통합 리포트 요청: userId={}, months={}", effectiveUserId, months);
        HealthReportResponse report = healthReportService.generateReport(effectiveUserId, months);
        return ResponseEntity.ok(ApiResponse.ok(report));
    }
}
