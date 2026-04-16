package com.medicatch.insurance.dto.response.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 정액형 계약 정보 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlatRateContract {

    private String resCompanyNm;         // 보험회사명
    private String resContractNo;        // 증권번호
    private String resProductNm;         // 상품명
    private String resPolicyHolder;      // 계약자명
    private String resInsuredNm;         // 피보험자명
    private String resContractStatus;    // 계약상태
    private String resContractDate;      // 계약일자
    private String resExpiryDate;        // 만기일자
    private String resMonthlyPremium;    // 월납보험료
    private String resTotalPremium;      // 총보험료
    private String resPaymentCycle;      // 납입주기
    private String resPaymentPeriod;     // 납입기간
    private String resCoveragePeriod;    // 보장기간
}
