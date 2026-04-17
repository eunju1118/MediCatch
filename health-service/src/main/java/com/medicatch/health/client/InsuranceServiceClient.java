package com.medicatch.health.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * insurance-service 내부 API Feign 클라이언트.
 *
 * <p>insurance-service가 Eureka에 "insurance-service" 이름으로 등록되어 있으므로
 * 로드밸런서를 통해 자동으로 라우팅된다.</p>
 */
@FeignClient(name = "insurance-service")
public interface InsuranceServiceClient {

    /**
     * 특정 유저의 최근 보험 계약정보 조회.
     *
     * <p>건강 통합 리포트의 보장 공백(coverageGap) 및 청구 가능 항목(insuranceClaimable) 분석에 사용.</p>
     *
     * @return contractData(InsuranceContractResponse), hasContractData 포함 맵
     */
    @GetMapping("/internal/contract-data/{userId}")
    Map<String, Object> getContractData(@PathVariable("userId") String userId);
}
