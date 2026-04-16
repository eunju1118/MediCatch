package com.medicatch.insurance.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 내보험다보여 계약정보 2차 인증 요청 DTO
 */
@Getter
@Setter
public class InsuranceContractCertifyRequest {

    @Valid
    @NotNull
    private InsuranceContractRequest original;

    @NotNull
    private Integer jobIndex;

    @NotNull
    private Integer threadIndex;

    @NotBlank
    private String jti;

    @NotNull
    private Long twoWayTimestamp;
}
