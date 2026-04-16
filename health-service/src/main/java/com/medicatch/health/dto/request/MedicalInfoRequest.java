package com.medicatch.health.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 내 진료정보 열람 요청 DTO
 *
 * CODEF API : /v1/kr/public/hw/hira-list/my-medical-information
 * organization: 0020
 */
@Getter
@Setter
public class MedicalInfoRequest {

    /**
     * 로그인 타입
     * - "2" : 인증서 / 휴대폰 인증 (기본값)
     * - "5" : 간편인증 (카카오, PASS, 네이버 등)
     */
    private String loginType = "2";

    /**
     * 로그인 세부 타입
     * - loginType = "2" 일 때 : "0" = 인증서, "1" = 휴대폰 (기본값)
     * - loginType = "5" 일 때 : "1" = 카카오, "3" = 삼성패스, "4" = KB모바일,
     *                           "5" = PASS, "6" = 네이버, "8" = toss, "10" = NH
     */
    private String loginTypeLevel = "1";

    /** 사용자 이름 */
    @NotBlank(message = "이름은 필수입니다")
    private String userName;

    /**
     * 주민번호 13자리 (하이픈 없이)
     * 서버에서 RSA 암호화 후 CODEF에 전송 — 평문으로 전달
     */
    @NotBlank(message = "주민번호는 필수입니다")
    private String identity;

    /** 휴대폰 번호 (010XXXXXXXX 형식, 하이픈 없이) */
    @NotBlank(message = "휴대폰 번호는 필수입니다")
    private String phoneNo;

    /**
     * 인증 방식
     * - "0" : SMS (기본값)
     * - "1" : PASS 앱 인증
     */
    private String authMethod = "0";

    /**
     * 통신사
     * - "0" : SKT (기본값)
     * - "1" : KT
     * - "2" : LGU+
     * - "3" : SKT 알뜰폰, "4" : KT 알뜰폰, "5" : LGU+ 알뜰폰
     */
    private String telecom = "0";

    /**
     * 조회 시작일 (yyyyMMdd)
     * 예: "20240101"
     */
    @NotBlank(message = "조회 시작일은 필수입니다")
    @Pattern(regexp = "\\d{8}", message = "시작일 형식은 yyyyMMdd 입니다")
    private String startDate;

    /**
     * 조회 종료일 (yyyyMMdd)
     * 예: "20241231"
     */
    @NotBlank(message = "조회 종료일은 필수입니다")
    @Pattern(regexp = "\\d{8}", message = "종료일 형식은 yyyyMMdd 입니다")
    private String endDate;

    /**
     * 민감상병 포함 여부
     * - "0" : 민감상병 미포함 (기본값)
     * - "1" : 포함 (정신질환, 감염병 등)
     */
    private String type = "0";
}
