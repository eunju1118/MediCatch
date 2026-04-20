//package com.medicatch.health.dto.response.medical;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
///**
// * 처방조제정보 (resPrescribeDrugList 항목)
// *
// * <p>처방전 발행 후 약국에서 조제된 약품 정보.</p>
// */
//@Getter
//@NoArgsConstructor
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class PrescribeDrug {
//
//    /** 처방일자 (yyyyMMdd) */
//    private String reqDate;
//
//    /** 약국명 */
//    private String resPharmNm;
//
//    /** 처방 의료기관명 */
//    private String resMedInstNm;
//
//    /** 약품코드 */
//    private String resDrugCd;
//
//    /** 약품명 */
//    private String resDrugNm;
//
//    /** 주성분 코드 */
//    private String resDrugCompCd;
//
//    /** 주성분명 */
//    private String resDrugCompNm;
//
//    /** 1회 투여량 */
//    private String resOneDosage;
//
//    /** 1일 투여횟수 */
//    private String resDayDosage;
//
//    /** 총 복용일수 */
//    private String resDosageDays;
//
//    /** 약품 단가 (원) */
//    private String resDrugUnitCost;
//
//    /** 본인부담금 (원) */
//    private String resPatPayment;
//}


package com.medicatch.health.dto.response.medical;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * 처방조제정보 (resPrescribeDrugList 항목)
 * CODEF 응답 키값 매핑 완료
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrescribeDrug {

    /** 처방(진료)일자 (yyyyMMdd) */
    @JsonProperty("resTreatStartDate")
    private String reqDate;

    /** 약국명 또는 진료구분 (명세상 resTreatType 매핑) */
    @JsonProperty("resTreatType")
    private String resPharmNm;

    /** 처방 의료기관명 */
    @JsonProperty("resHospitalName")
    private String resMedInstNm;

    /** 약품코드 (명세에 없을 경우 null 처리됨) */
    @JsonProperty("resDrugCode")
    private String resDrugCd;

    /** 약품명 */
    @JsonProperty("resDrugName")
    private String resDrugNm;

    /** 주성분 코드 */
    @JsonProperty("resIngredientsCode")
    private String resDrugCompCd;

    /** 주성분명 */
    @JsonProperty("resIngredients")
    private String resDrugCompNm;

    /** 1회 투여량 */
    @JsonProperty("resOneDose")
    private String resOneDosage;

    /** 1일 투여횟수 */
    @JsonProperty("resDailyDosesNumber")
    private String resDayDosage;

    /** 총 복용일수 */
    @JsonProperty("resTotalDosingdays")
    private String resDosageDays;

    /** 약품 단가 (원) */
    @JsonProperty("resDrugUnitCost")
    private String resDrugUnitCost;

    /** 본인부담금 (원) */
    @JsonProperty("resDeductibleAmt")
    private String resPatPayment;
}