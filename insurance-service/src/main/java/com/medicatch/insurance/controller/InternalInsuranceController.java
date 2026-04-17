package com.medicatch.insurance.controller;

import com.medicatch.insurance.store.InsuranceDataStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * health-service가 Feign으로 호출하는 내부 전용 컨트롤러.
 *
 * <p>외부(게이트웨이)에 노출되지 않으며, 서비스 간 통신 전용이다.</p>
 */
@Slf4j
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalInsuranceController {

    private final InsuranceDataStore insuranceDataStore;

    /**
     * 특정 유저의 최근 보험 계약정보 반환.
     *
     * <p>health-service가 건강 통합 리포트의 보장 공백 분석에 사용한다.</p>
     */
    @GetMapping("/contract-data/{userId}")
    public ResponseEntity<Map<String, Object>> getContractData(@PathVariable String userId) {
        log.debug("내부 계약정보 조회: userId={}", userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("contractData",    insuranceDataStore.getContractData(userId).orElse(null));
        result.put("hasContractData", insuranceDataStore.hasContractData(userId));

        return ResponseEntity.ok(result);
    }
}
