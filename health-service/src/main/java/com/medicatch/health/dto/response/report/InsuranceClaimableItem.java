package com.medicatch.health.dto.response.report;

import lombok.Builder;
import lombok.Getter;

/**
 * 보험 청구 가능 항목.
 *
 * <p>실손 또는 정액 계약이 존재하고 환자 본인부담금이 발생한 진료 건.</p>
 */
@Getter
@Builder
public class InsuranceClaimableItem {

    /** 진료일 (yyyy-MM-dd) */
    private final String treatmentDate;

    /** 의료기관명 */
    private final String hospitalName;

    /** 진료과 */
    private final String department;

    /** 질병코드 (ICD-10) */
    private final String diseaseCode;

    /** 질병명 */
    private final String diseaseCodeName;

    /** 본인부담금 (원) */
    private final long patientPayment;

    /** 청구 가능 사유 */
    private final String claimableReason;

    /** 적용 계약 유형 ("실손" | "정액") */
    private final String contractType;
}
