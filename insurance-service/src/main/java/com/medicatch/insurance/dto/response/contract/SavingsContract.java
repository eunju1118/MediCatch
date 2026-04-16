package com.medicatch.insurance.dto.response.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 저축성 계약 정보 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SavingsContract {

    private String resCompanyNm;
    private String resContractNo;
    private String resProductNm;
    private String resPolicyHolder;
    private String resInsuredNm;
    private String resContractStatus;
    private String resContractDate;
    private String resExpiryDate;
    private String resMonthlyPremium;
    private String resTotalPremium;
    private String resPaymentCycle;
    private String resPaymentPeriod;
    private String resCoveragePeriod;
    private String resAccumulatedAmt;    // 적립액
    private String resSurrenderValue;    // 해지환급금
}
