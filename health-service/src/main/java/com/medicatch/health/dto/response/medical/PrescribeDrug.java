package com.medicatch.health.dto.response.medical;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 처방조제정보 (resPrescribeDrugList 항목)
 *
 * <p>처방전 발행 후 약국에서 조제된 약품 정보.</p>
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrescribeDrug {

    /** 처방일자 (yyyyMMdd) */
    private String reqDate;

    /** 약국명 */
    private String resPharmNm;

    /** 처방 의료기관명 */
    private String resMedInstNm;

    /** 약품코드 */
    private String resDrugCd;

    /** 약품명 */
    private String resDrugNm;

    /** 주성분 코드 */
    private String resDrugCompCd;

    /** 주성분명 */
    private String resDrugCompNm;

    /** 1회 투여량 */
    private String resOneDosage;

    /** 1일 투여횟수 */
    private String resDayDosage;

    /** 총 복용일수 */
    private String resDosageDays;

    /** 약품 단가 (원) */
    private String resDrugUnitCost;

    /** 본인부담금 (원) */
    private String resPatPayment;
}
