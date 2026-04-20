package com.medicatch.health.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 진료기록 조회 요청 DTO
 * CODEF API: /v1/kr/public/hw/hira-list/my-medical-information
 * organization: 0020
 */
@Getter
@Setter
public class MedicalRequest {

    @NotBlank
    private String userName;        // 사용자 이름

    @NotBlank
    private String identity;        // 주민번호 (RSA 암호화 처리)

    @NotBlank
    private String phoneNo;         // 휴대폰 번호

    private String telecom = "0";   // 통신사: 0=SKT, 1=KT, 2=LGU+

    private String loginType = "5"; // 2=인증서/휴대폰, 5=간편인증

    private String loginTypeLevel = "1"; // 2인경우: 0=인증서, 1=휴대폰

    private String authMethod = "0"; // 0=SMS, 1=PASS

    @NotBlank
    private String startDate;       // 조회 시작일 (yyyyMMdd)

    @NotBlank
    private String endDate;         // 조회 종료일 (yyyyMMdd)

    private String type = "0";      // 0=민감상병 미포함, 1=포함
}
