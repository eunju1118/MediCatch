package com.medicatch.chat.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * health-service 내부 API Feign 클라이언트.
 *
 * <p>health-service가 Eureka에 "health-service" 이름으로 등록되어 있으므로
 * 로드밸런서를 통해 자동으로 라우팅된다.</p>
 */
@FeignClient(name = "health-service")
public interface HealthServiceClient {

    /**
     * 특정 유저의 최근 진료기록 + 건강검진 데이터 조회.
     *
     * @param userId Gateway에서 JWT로부터 추출한 사용자 ID
     * @return medicalData, checkupData, hasMedicalData, hasCheckupData 포함 맵
     */
    @GetMapping("/internal/health-data/{userId}")
    Map<String, Object> getHealthData(@PathVariable("userId") String userId);
}
