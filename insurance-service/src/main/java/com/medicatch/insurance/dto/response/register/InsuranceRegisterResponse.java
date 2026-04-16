package com.medicatch.insurance.dto.response.register;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 내보험다보여 회원가입 신청 응답 DTO
 *
 * <p>CODEF API data[0] 항목을 매핑한다.</p>
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InsuranceRegisterResponse {

    /** 가입상태 (예: 가입완료, 처리중) */
    private String resRegistrationStatus;

    /** 발급된 로그인 ID */
    private String resLoginId;

    /** 등록된 이메일 */
    private String resEmail;
}
