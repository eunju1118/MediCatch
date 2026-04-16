package com.medicatch.health.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * CODEF 2-Way 인증 공통 요청 DTO
 * 1차 요청 후 CF-03002 응답 수신 시 사용
 */
@Getter
@Setter
public class TwoWayRequest {

    @NotBlank
    private String productUrl;      // 원본 API URL

    @NotNull
    private Integer jobIndex;

    @NotNull
    private Integer threadIndex;

    @NotBlank
    private String jti;             // 트랜잭션 ID

    @NotNull
    private Long twoWayTimestamp;
}
