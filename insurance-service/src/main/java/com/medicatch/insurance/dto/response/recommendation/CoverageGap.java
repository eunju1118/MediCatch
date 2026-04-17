package com.medicatch.insurance.dto.response.recommendation;

import lombok.Builder;
import lombok.Getter;

/**
 * 보장 공백 항목.
 *
 * <p>사용자가 실제 진료를 받은 진료과이지만,
 * 현재 보험 계약으로 해당 영역이 보장되지 않는 경우.</p>
 */
@Getter
@Builder
public class CoverageGap {

    /** 진료과명 */
    private final String department;

    /** 분석 기간 내 해당 진료과 방문 횟수 */
    private final int visitCount;

    /** 본인부담금 합계 (원) */
    private final long totalPatientPayment;

    /** 공백 유형 (DENTAL / MENTAL_HEALTH / ORIENTAL_MEDICINE / OPHTHALMOLOGY / DERMATOLOGY) */
    private final String gapType;

    /** 보장 공백 설명 */
    private final String description;

    /** 이 공백을 메울 추천 보험 유형 */
    private final String recommendedInsuranceType;
}
