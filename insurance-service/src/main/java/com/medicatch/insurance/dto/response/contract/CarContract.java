package com.medicatch.insurance.dto.response.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 자동차보험 계약 정보 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CarContract {

    private String resCompanyNm;
    private String resContractNo;
    private String resProductNm;
    private String resPolicyHolder;
    private String resInsuredNm;
    private String resContractStatus;
    private String resContractDate;
    private String resExpiryDate;
    private String resMonthlyPremium;
    private String resCarNo;             // 차량번호
    private String resCarType;           // 차종
    private String resCarModel;          // 차량모델명
    private String resCarYear;           // 연식
}
