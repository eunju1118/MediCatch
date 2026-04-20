//package com.medicatch.health.dto.response.medical;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
///**
// * 세부진료정보 (resDetailTreatList 항목)
// *
// * <p>영상진단, 수술, 처치 등 진료 항목별 상세 정보.</p>
// */
//@Getter
//@NoArgsConstructor
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class DetailTreat {
//
//    /** 진료일자 (yyyyMMdd) */
//    private String reqDate;
//
//    /** 의료기관명 */
//    private String resMedInstNm;
//
//    /** 진료행위 분류 코드 */
//    private String resItemCd;
//
//    /** 진료행위 항목명 */
//    private String resItemCdNm;
//
//    /** 항목 수량 */
//    private String resItemCount;
//
//    /** 항목 단가 (원) */
//    private String resItemUnitCost;
//
//    /** 항목 금액 (원) */
//    private String resItemCost;
//
//    /** 본인부담금 (원) */
//    private String resPatPayment;
//
//    /** 급여 구분 (급여/비급여) */
//    private String resBenefitDivCdNm;
//}


package com.medicatch.health.dto.response.medical;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 세부진료정보 (resDetailTreatList 항목)
 * * CODEF 응답 키값에 맞춰 필드 매핑 완료
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailTreat {

    /** 진료일자 (yyyyMMdd) */
    @JsonProperty("resTreatStartDate")
    private String reqDate;

    /** 의료기관명 */
    @JsonProperty("resHospitalName")
    private String resMedInstNm;

    /** 진료행위 분류 코드 -> 명세상 resCodeName 매핑 */
    @JsonProperty("resCodeName")
    private String resItemCd;

    /** 진료행위 항목명 -> 명세상 resTreatType 매핑 */
    @JsonProperty("resTreatType")
    private String resItemCdNm;

    /** 항목 수량 (1회 투약량) */
    @JsonProperty("resOneDose")
    private String resItemCount;

    /** 항목 단가 -> 명세상 1일 투약횟수 매핑 */
    @JsonProperty("resDailyDosesNumber")
    private String resItemUnitCost;

    /** 항목 금액 -> 명세상 총 투약일수 매핑 */
    @JsonProperty("resTotalDosingdays")
    private String resItemCost;

    /** 본인부담금 (원) - 상세 내역 JSON에 없을 경우 대비 */
    @JsonProperty("resDeductibleAmt")
    private String resPatPayment;

    /** 급여 구분 (급여/비급여) */
    @JsonProperty("resBenefitDivCdNm")
    private String resBenefitDivCdNm;
}