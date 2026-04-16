package com.medicatch.health.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 내 진료정보 열람 2차 인증 요청 DTO
 *
 * <p>1차 요청에서 CF-03002 응답을 받은 후, 사용자가 간편인증을 완료했을 때 호출한다.
 * 원본 요청 파라미터와 2-Way 인증 메타데이터를 함께 담는다.</p>
 *
 * <p>요청 예시:</p>
 * <pre>
 * POST /api/health/medical/info/certify
 * {
 *   "original": {
 *     "userName": "홍길동",
 *     "identity": "9001011234567",
 *     "phoneNo":  "01012345678",
 *     "startDate": "20240101",
 *     "endDate":   "20241231"
 *   },
 *   "jobIndex":        0,
 *   "threadIndex":     0,
 *   "jti":             "트랜잭션ID",
 *   "twoWayTimestamp": 1234567890000
 * }
 * </pre>
 */
@Getter
@Setter
public class MedicalInfoCertifyRequest {

    /** 1차 요청과 동일한 원본 파라미터 (CODEF params 재구성에 사용) */
    @NotNull(message = "원본 요청 정보는 필수입니다")
    @Valid
    private MedicalInfoRequest original;

    /** 1차 응답 data.jobIndex */
    @NotNull(message = "jobIndex는 필수입니다")
    private Integer jobIndex;

    /** 1차 응답 data.threadIndex */
    @NotNull(message = "threadIndex는 필수입니다")
    private Integer threadIndex;

    /** 1차 응답 data.jti — 트랜잭션 고유 식별자 */
    @NotBlank(message = "jti는 필수입니다")
    private String jti;

    /** 1차 응답 data.twoWayTimestamp */
    @NotNull(message = "twoWayTimestamp는 필수입니다")
    private Long twoWayTimestamp;
}
