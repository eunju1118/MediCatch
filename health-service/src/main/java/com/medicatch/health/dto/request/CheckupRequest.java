package com.medicatch.health.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 건강검진결과 조회 요청 DTO
 * CODEF API: /v1/kr/health/0002/nhis/health-checkup
 * organization: 0002
 */
@Getter
@Setter
public class CheckupRequest {

    private String loginType = "5";     // 5=간편인증 (기본값)

    private String loginTypeLevel = "1"; // 1=카카오, 3=삼성패스, 5=PASS 등

    @NotBlank
    private String identity;            // 주민번호 (RSA 암호화)

    @NotBlank
    private String birthDate;           // 생년월일 (yymmdd)

    private String certType = "1";      // 인증서 타입 (간편인증 시 불필요)

    private String inquiryType = "0";   // 0=일반, 1=상세+PDF, 4=문진JSON

    @NotBlank
    private String searchStartYear;     // 검색 시작 연도 (yyyy)

    @NotBlank
    private String searchEndYear;       // 검색 종료 연도 (yyyy)

    private String type = "0";          // 0=전체, 1=본인, 2=영유아
}
