package com.medicatch.insurance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 내보험다보여 회원가입 요청 DTO
 * CODEF API: /v1/kr/insurance/0001/credit4u/register
 */
@Getter
@Setter
public class InsuranceRegisterRequest {

    @NotBlank
    private String userName;        // 사용자 이름

    @NotBlank
    private String identity;        // 주민번호 (RSA 암호화)

    private String telecom = "0";   // 통신사: 0=SKT, 1=KT, 2=LGU+

    @NotBlank
    private String phoneNo;         // 휴대폰 번호

    private String authMethod = "0"; // 0=SMS, 1=PASS

    private String type = "0";      // 0=바로 회원가입, 1=유효성 체크 후 회원가입

    @NotBlank
    private String id;              // 영문+숫자 6~12자

    @NotBlank
    private String password;        // RSA 암호화 비밀번호 (9~20자, 영문+숫자+특수문자)

    @NotBlank
    private String email;           // 이메일 (허용 도메인: naver.com 등)
}
