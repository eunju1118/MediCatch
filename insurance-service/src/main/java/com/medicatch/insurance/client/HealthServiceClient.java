package com.medicatch.insurance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * health-service 내부 API 클라이언트.
 *
 * <p>GET /internal/health-data/{userId} 응답 구조:</p>
 * <pre>
 * {
 *   "medicalData":    { "resBasicTreatList": [...] },
 *   "checkupData":    { ... },
 *   "hasMedicalData": true,
 *   "hasCheckupData": true
 * }
 * </pre>
 */
@FeignClient(name = "health-service")
public interface HealthServiceClient {

    @GetMapping("/internal/health-data/{userId}")
    Map<String, Object> getHealthData(@PathVariable("userId") String userId);
}
