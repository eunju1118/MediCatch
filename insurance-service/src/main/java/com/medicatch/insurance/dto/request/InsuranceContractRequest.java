package com.medicatch.insurance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 내보험다보여 계약정보 조회 요청 DTO
 * CODEF API: /v1/kr/insurance/0001/credit4u/contract-info
 */
@Getter
@Setter
public class InsuranceContractRequest {

    @NotBlank
    private String id;               // 내보험다보여 로그인 ID

    @NotBlank
    private String password;         // RSA 암호화 전 비밀번호

    @NotBlank
    private String identity;         // 주민번호 13자리 (RSA 암호화 후 전송)

    /**
     * 보험 유형
     * 0=전체, 1=정액형, 2=실손형, 3=분석통계,
     * 4=실손지급, 5=화재특종, 6=자동차, 7=저축성
     */
    private String type = "0";

    @NotBlank
    private String userName;

    @NotBlank
    private String phoneNo;

    private String telecom = "0";    // 0=SKT, 1=KT, 2=LGU+

    private String authMethod = "0"; // 0=SMS, 1=PASS앱
}
