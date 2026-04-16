package com.medicatch.insurance.dto.response.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 내보험다보여 계약정보 조회 응답 DTO
 *
 * <p>CODEF API data[0] 항목을 매핑한다.</p>
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InsuranceContractResponse {

    /** 정액형 계약 목록 */
    private List<FlatRateContract> resFlatRateContractList;

    /** 실손형 계약 목록 */
    private List<ActualLossContract> resActualLossContractList;

    /** 실손 지급 내역 목록 */
    private List<ActualLossPayment> resActualLossPaymentList;

    /** 자동차보험 계약 목록 */
    private List<CarContract> resCarContractList;

    /** 저축성 계약 목록 */
    private List<SavingsContract> resSavingsContractList;

    /** 정액형 분석통계 */
    private List<FlatRateStatistics> resFlatRateStatisticsList;

    /** 실손형 분석통계 */
    private List<ActualLossStatistics> resActualLossStatisticsList;
}
