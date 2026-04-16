package com.medicatch.insurance.dto.response.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 실손형 계약 정보 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActualLossContract {

    private String resCompanyNm;
    private String resContractNo;
    private String resProductNm;
    private String resPolicyHolder;
    private String resInsuredNm;
    private String resContractStatus;
    private String resContractDate;
    private String resExpiryDate;
    private String resMonthlyPremium;
    private String resPaymentCycle;
    private String resPaymentPeriod;
    private String resCoveragePeriod;
    private String resActualLossType;    // 실손 유형 (표준형/비표준형 등)
    private String resSpecialClause;     // 특약
}
