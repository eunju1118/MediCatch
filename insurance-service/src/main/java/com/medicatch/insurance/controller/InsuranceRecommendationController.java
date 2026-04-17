package com.medicatch.insurance.controller;

import com.medicatch.insurance.dto.response.ApiResponse;
import com.medicatch.insurance.dto.response.recommendation.InsuranceRecommendation;
import com.medicatch.insurance.service.InsuranceRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 보험 추천 컨트롤러.
 *
 * <p>API Gateway 경유 실제 경로:</p>
 * <ul>
 *   <li>{@code GET /api/insurance/recommend?userId={userId}}</li>
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
 *     "generatedAt": "2024-03-15T10:30:00",
 *     "coverageGaps": [
 *       { "department": "치과", "visitCount": 3, "totalPatientPayment": 120000,
 *         "gapType": "DENTAL", "description": "...", "recommendedInsuranceType": "치아보험(치과보험)" }
 *     ],
 *     "recommendedTypes": ["치아보험(치과보험)", "한방 특약 실손보험"],
 *     "aiMessage": "GPT가 생성한 맞춤 추천 메시지...",
 *     "analyzedTreatmentCount": 15,
 *     "currentContractCount": 2,
 *     "healthDataAvailable": true,
 *     "insuranceDataAvailable": true
 *   }
 * }
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class InsuranceRecommendationController {

    private final InsuranceRecommendationService recommendationService;

    /**
     * 보험 추천 조회.
     *
     * @param userId       쿼리 파라미터 userId (선택 — X-User-Id 헤더가 우선)
     * @param headerUserId Gateway가 주입한 X-User-Id 헤더
     */
    @GetMapping
    public ResponseEntity<ApiResponse<InsuranceRecommendation>> recommend(
            @RequestParam(required = false) String userId,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {

        String effectiveUserId = (headerUserId != null && !headerUserId.isBlank())
                ? headerUserId : userId;

        if (effectiveUserId == null || effectiveUserId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("userId가 필요합니다 (X-User-Id 헤더 또는 userId 파라미터)"));
        }

        log.debug("보험 추천 요청: userId={}", effectiveUserId);
        InsuranceRecommendation result = recommendationService.recommend(effectiveUserId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
