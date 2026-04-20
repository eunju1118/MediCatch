//package com.medicatch.health.dto.response.medical;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
///**
// * 기본진료내역 (resBasicTreatList 항목)
// *
// * <p>병원 방문 1건당 하나의 항목으로 구성된다.</p>
// */
//@Getter
//@NoArgsConstructor
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class BasicTreat {
//
//    /** 진료일자 (yyyyMMdd) */
//    private String reqDate;
//
//    /** 의료기관명 */
//    private String resMedInstNm;
//
//    /** 진료과 코드 */
//    private String resDeptCd;
//
//    /** 진료과명 */
//    private String resDeptCdNm;
//
//    /** 질병코드 (ICD-10) */
//    private String resDissCd;
//
//    /** 질병명 */
//    private String resDissCdNm;
//
//    /** 총 진료비 (원) */
//    private String resTotalCost;
//
//    /** 본인부담금 (원) */
//    private String resPatPayment;
//
//    /** 공단부담금 (원) */
//    private String resInsurancePayment;
//
//    /** 진료일수 */
//    private String resTreatDays;
//
//    /** 입내원 구분 (1: 입원, 2: 외래, 3: 약국) */
//    private String resInOutPatientDivCd;
//
//    /** 입내원 구분명 */
//    private String resInOutPatientDivCdNm;
//}
package com.medicatch.health.dto.response.medical;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기본진료내역 (resBasicTreatList 항목)
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasicTreat {

    /** 진료일자 (yyyyMMdd) */
    @JsonProperty("resTreatStartDate")
    private String reqDate;

    /** 의료기관명 */
    @JsonProperty("resHospitalName")
    private String resMedInstNm;

    /** 진료과 코드 */
    @JsonProperty("resHospitalCode") // 또는 명세에 맞는 코드 필드
    private String resDeptCd;

    /** 진료과명 */
    @JsonProperty("resDepartment")
    private String resDeptCdNm;

    /** 질병코드 (ICD-10) */
    @JsonProperty("resDiseaseCode")
    private String resDissCd;

    /** 질병명 */
    @JsonProperty("resDiseaseName")
    private String resDissCdNm;

    /** 총 진료비 (원) */
    @JsonProperty("resTotalAmount")
    private String resTotalCost;

    /** 본인부담금 (원) */
    @JsonProperty("resDeductibleAmt")
    private String resPatPayment;

    /** 공단부담금 (원) */
    @JsonProperty("resPublicCharge")
    private String resInsurancePayment;

    /** 진료일수 */
    @JsonProperty("resVisitDays")
    private String resTreatDays;

    /** 입내원 구분명 (예: 외래, 입원) */
    @JsonProperty("resTreatType")
    private String resInOutPatientDivCdNm;
}