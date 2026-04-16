package com.medicatch.insurance.dto.response.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 정액형 분석통계 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlatRateStatistics {

    private String resInsuranceType;     // 보험 유형
    private String resContractCount;     // 계약 건수
    private String resTotalMonthlyPremium; // 월납 총보험료
    private String resTotalCoverage;     // 총 보장금액
}
