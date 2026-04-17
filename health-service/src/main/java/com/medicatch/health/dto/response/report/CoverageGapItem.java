package com.medicatch.health.dto.response.report;

import lombok.Builder;
import lombok.Getter;

/**
 * 보장 공백 항목.
 *
 * <p>진료 기록에는 있으나 현재 보험 계약으로 보장되지 않는 진료과/영역.</p>
 */
@Getter
@Builder
public class CoverageGapItem {

    /** 진료과 */
    private final String department;

    /** 방문 횟수 */
    private final int visitCount;

    /** 총 진료비 (원) */
    private final long totalCost;

    /** 총 본인부담금 (원) */
    private final long totalPatientPayment;

    /** 공백 유형 (DENTAL / MENTAL_HEALTH / ORIENTAL_MEDICINE / OPHTHALMOLOGY / DERMATOLOGY) */
    private final String gapType;

    /** 공백 설명 */
    private final String gapDescription;

    /** 추천 보험 상품 유형 */
    private final String recommendation;
}
