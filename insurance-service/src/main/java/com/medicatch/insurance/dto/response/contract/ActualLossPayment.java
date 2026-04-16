package com.medicatch.insurance.dto.response.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 실손 지급 내역 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActualLossPayment {

    private String resCompanyNm;
    private String resContractNo;
    private String resPaymentDate;       // 지급일자
    private String resPaymentAmt;        // 지급금액
    private String resPaymentType;       // 지급유형
    private String resMedInstNm;         // 의료기관명
    private String resTreatmentDate;     // 진료일자
    private String resClaimNo;           // 청구번호
}
