//package com.medicatch.insurance.dto.response.contract;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
///** 실손형 계약 정보 */
//@Getter
//@NoArgsConstructor
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class ActualLossContract {
//
//    private String resCompanyNm;
//    private String resContractNo;
//    private String resProductNm;
//    private String resPolicyHolder;
//    private String resInsuredNm;
//    private String resContractStatus;
//    private String resContractDate;
//    private String resExpiryDate;
//    private String resMonthlyPremium;
//    private String resPaymentCycle;
//    private String resPaymentPeriod;
//    private String resCoveragePeriod;
//    private String resActualLossType;    // 실손 유형 (표준형/비표준형 등)
//    private String resSpecialClause;     // 특약
//}

package com.medicatch.insurance.dto.response.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/** 실손형 계약 정보 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActualLossContract {

    private String resCompanyNm;         // 보험회사명
    private String resPolicyNumber;      // 증권번호
    private String resInsuranceName;     // 상품명
    private String resInsuredPerson;     // 피보험자명 (기존 resInsuredNm 변경)
    private String resContractStatus;    // 계약상태 (보통 '정' 또는 '정상')

    /** * 실손형은 날짜(commStartDate, commEndDate)가 이 리스트 안에 들어있습니다.
     * 프론트엔드에서 이 리스트의 첫 번째 요소를 참조하게 됩니다.
     */
    private List<Map<String, Object>> resCoverageLists;
}