package com.medicatch.health.dto.response.medical;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailTreat {

    /** 진료일자 (yyyyMMdd) */
    private String resTreatStartDate;

    /** 의료기관명 */
    private String resHospitalName;

    /** 진료구분 */
    private String resTreatType;

    /** 진료행위명 */
    private String resCodeName;

    /** 1회 투여량 */
    private String resOneDose;

    /** 1일 투여횟수 */
    private String resDailyDosesNumber;

    /** 총 투여일수 */
    private String resTotalDosingdays;
}