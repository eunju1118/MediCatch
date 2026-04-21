//package com.medicatch.insurance.dto.response.contract;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
///** 정액형 계약 정보 */
//@Getter
//@NoArgsConstructor
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class FlatRateContract {
//
//    private String resCompanyNm;         // 보험회사명
//    private String resContractNo;        // 증권번호
//    private String resProductNm;         // 상품명
//    private String resPolicyHolder;      // 계약자명
//    private String resInsuredNm;         // 피보험자명
//    private String resContractStatus;    // 계약상태
//    private String resContractDate;      // 계약일자
//    private String resExpiryDate;        // 만기일자
//    private String resMonthlyPremium;    // 월납보험료
//    private String resTotalPremium;      // 총보험료
//    private String resPaymentCycle;      // 납입주기
//    private String resPaymentPeriod;     // 납입기간
//    private String resCoveragePeriod;    // 보장기간
//}


package com.medicatch.insurance.dto.response.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/** 정액형 계약 정보 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlatRateContract {

    private String resCompanyNm;         // 보험회사명
    private String resPolicyNumber;      // 증권번호 (기존 resContractNo 변경)
    private String resInsuranceName;     // 상품명 (기존 resProductNm 변경)
    private String resContractor;        // 계약자명 (기존 resPolicyHolder 변경)
    private String resContractStatus;    // 계약상태
    private String commStartDate;        // 계약일자 (기존 resContractDate 변경)
    private String commEndDate;          // 만기일자 (기존 resExpiryDate 변경)
    private String resPremium;           // 보험료 (기존 resMonthlyPremium 변경)
    private String resPaymentCycle;      // 납입주기
    private String resPaymentPeriod;     // 납입기간

    // 상세 보장 내역이 필요한 경우를 위해 추가
    private List<Map<String, Object>> resCoverageLists;
}