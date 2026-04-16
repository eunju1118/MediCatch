package com.medicatch.insurance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 내보험다보여 회원가입 신청 요청 DTO
 * CODEF API: /v1/kr/insurance/0001/credit4u/register
 */
@Getter
@Setter
public class InsuranceRegisterRequest {

    @NotBlank
    private String userName;

    @NotBlank
    private String identity;         // 주민번호 13자리 (RSA 암호화 후 전송)

    @NotBlank
    @Pattern(regexp = "\\d{8}", message = "생년월일은 8자리 숫자(YYYYMMDD)여야 합니다")
    private String birthDate;        // 생년월일 (YYYYMMDD)

    private String telecom = "0";    // 0=SKT, 1=KT, 2=LGU+

    @NotBlank
    private String phoneNo;

    private String authMethod = "0"; // 0=SMS, 1=PASS앱

    private String type = "0";       // 0=바로 회원가입, 1=유효성 체크 후 회원가입

    @NotBlank
    private String id;               // 영문+숫자 6~12자

    @NotBlank
    private String password;         // RSA 암호화 전 비밀번호 (9~20자, 영문+숫자+특수문자)

    @NotBlank
    private String email;
}
