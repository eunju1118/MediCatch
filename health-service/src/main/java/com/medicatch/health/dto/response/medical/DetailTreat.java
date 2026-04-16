package com.medicatch.health.dto.response.medical;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 세부진료정보 (resDetailTreatList 항목)
 *
 * <p>영상진단, 수술, 처치 등 진료 항목별 상세 정보.</p>
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailTreat {

    /** 진료일자 (yyyyMMdd) */
    private String reqDate;

    /** 의료기관명 */
    private String resMedInstNm;

    /** 진료행위 분류 코드 */
    private String resItemCd;

    /** 진료행위 항목명 */
    private String resItemCdNm;

    /** 항목 수량 */
    private String resItemCount;

    /** 항목 단가 (원) */
    private String resItemUnitCost;

    /** 항목 금액 (원) */
    private String resItemCost;

    /** 본인부담금 (원) */
    private String resPatPayment;

    /** 급여 구분 (급여/비급여) */
    private String resBenefitDivCdNm;
}
