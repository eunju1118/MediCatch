package com.medicatch.health.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 건강검진결과 조회 2차 인증 요청 DTO
 *
 * <p>CheckupController의 /certify 엔드포인트에서 사용.
 * 원본 요청 파라미터 + 2-Way 메타데이터를 하나의 바디로 수신한다.</p>
 */
@Getter
@Setter
public class CheckupCertifyRequest {

    @Valid
    @NotNull
    private CheckupRequest original;

    @NotNull
    private Integer jobIndex;

    @NotNull
    private Integer threadIndex;

    @NotBlank
    private String jti;

    @NotNull
    private Long twoWayTimestamp;
}
