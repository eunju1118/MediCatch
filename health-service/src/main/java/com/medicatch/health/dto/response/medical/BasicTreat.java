package com.medicatch.health.dto.response.medical;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasicTreat {

    /** 진료일자 (yyyyMMdd) */
    private String resTreatStartDate;

    /** 의료기관명 */
    private String resHospitalName;

    /** 진료구분 (입원/외래 등) */
    private String resTreatType;

    /** 진료과명 */
    private String resDepartment;

    /** 질병코드 (ICD-10) */
    private String resDiseaseCode;

    /** 질병명 */
    private String resDiseaseName;

    /** 방문일수 */
    private String resVisitDays;

    /** 총진료비 (원) */
    private String resTotalAmount;

    /** 공단부담금 (원) */
    private String resPublicCharge;

    /** 본인부담금 (원) */
    private String resDeductibleAmt;

    /** 요양기관코드 */
    private String resHospitalCode;
}