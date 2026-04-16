package com.medicatch.insurance.dto.response.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 실손형 분석통계 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActualLossStatistics {

    private String resInsuranceType;
    private String resContractCount;
    private String resTotalMonthlyPremium;
    private String resActualLossType;    // 실손 유형
    private String resStandardType;      // 표준/비표준 구분
}
