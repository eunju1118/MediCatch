package com.medicatch.insurance.dto.response.recommendation;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 보험 추천 결과 응답 DTO.
 *
 * <p>사용자의 진료 기록과 현재 보험 계약을 교차 분석하여
 * GPT-4o가 생성한 맞춤형 보험 추천 정보를 담는다.</p>
 */
@Getter
@Builder
public class InsuranceRecommendation {

    /** 분석 대상 사용자 ID */
    private final String userId;

    /** 추천 생성 시각 */
    private final LocalDateTime generatedAt;

    /** 감지된 보장 공백 항목 리스트 (본인부담금 내림차순) */
    private final List<CoverageGap> coverageGaps;

    /** 추천 보험 유형 목록 (예: ["치아보험", "정신건강 특약 실손"]) */
    private final List<String> recommendedTypes;

    /** GPT-4o가 생성한 맞춤형 추천 메시지 */
    private final String aiMessage;

    /** 분석에 사용된 진료 건수 */
    private final int analyzedTreatmentCount;

    /** 현재 가입 보험 건수 (실손 + 정액 합산) */
    private final int currentContractCount;

    /** 건강 데이터 조회 가능 여부 */
    private final boolean healthDataAvailable;

    /** 보험 계약 데이터 조회 가능 여부 */
    private final boolean insuranceDataAvailable;
}
