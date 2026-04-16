package com.medicatch.insurance.dto.response.contract;

import com.medicatch.insurance.codef.TwoWayContext;
import lombok.Builder;
import lombok.Getter;

/**
 * 내보험다보여 계약정보 조회 서비스 반환 타입
 *
 * <pre>
 * twoWayRequired = true  → twoWayContext 참조 후 사용자 간편인증, /contract/certify 호출
 * twoWayRequired = false → contractInfo에 계약 목록 포함
 * </pre>
 */
@Getter
@Builder
public class InsuranceContractResult {

    private final boolean twoWayRequired;
    private final TwoWayContext twoWayContext;
    private final InsuranceContractResponse contractInfo;
    private final String resultCode;
    private final String resultMessage;

    public static InsuranceContractResult pending(TwoWayContext ctx,
                                                  String resultCode,
                                                  String resultMessage) {
        return InsuranceContractResult.builder()
                .twoWayRequired(true)
                .twoWayContext(ctx)
                .resultCode(resultCode)
                .resultMessage(resultMessage)
                .build();
    }

    public static InsuranceContractResult success(InsuranceContractResponse contractInfo) {
        return InsuranceContractResult.builder()
                .twoWayRequired(false)
                .contractInfo(contractInfo)
                .resultCode("CF-00000")
                .resultMessage("성공")
                .build();
    }
}
