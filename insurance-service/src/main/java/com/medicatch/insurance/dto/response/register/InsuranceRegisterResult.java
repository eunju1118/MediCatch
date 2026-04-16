package com.medicatch.insurance.dto.response.register;

import com.medicatch.insurance.codef.TwoWayContext;
import lombok.Builder;
import lombok.Getter;

/**
 * 내보험다보여 회원가입 신청 서비스 반환 타입
 *
 * <pre>
 * twoWayRequired = true  → twoWayContext 참조 후 사용자 간편인증, /register/certify 호출
 * twoWayRequired = false → registerInfo에 가입 결과 포함
 * </pre>
 */
@Getter
@Builder
public class InsuranceRegisterResult {

    private final boolean twoWayRequired;
    private final TwoWayContext twoWayContext;
    private final InsuranceRegisterResponse registerInfo;
    private final String resultCode;
    private final String resultMessage;

    public static InsuranceRegisterResult pending(TwoWayContext ctx,
                                                  String resultCode,
                                                  String resultMessage) {
        return InsuranceRegisterResult.builder()
                .twoWayRequired(true)
                .twoWayContext(ctx)
                .resultCode(resultCode)
                .resultMessage(resultMessage)
                .build();
    }

    public static InsuranceRegisterResult success(InsuranceRegisterResponse registerInfo) {
        return InsuranceRegisterResult.builder()
                .twoWayRequired(false)
                .registerInfo(registerInfo)
                .resultCode("CF-00000")
                .resultMessage("성공")
                .build();
    }
}
