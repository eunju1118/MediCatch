package com.medicatch.health.controller;

import com.medicatch.health.store.HealthDataStore;
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
 * chat-service가 Feign으로 호출하는 내부 전용 컨트롤러.
 *
 * <p>외부(게이트웨이)에 노출되지 않으며, 서비스 간 통신 전용이다.</p>
 * <p>경로: {@code GET /internal/health-data/{userId}}</p>
 */
@Slf4j
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalHealthController {

    private final HealthDataStore healthDataStore;

    /**
     * 특정 유저의 최근 건강 데이터 반환.
     *
     * <p>chat-service가 GPT 시스템 프롬프트 컨텍스트를 구성할 때 사용한다.
     * 데이터가 없으면 null 값이 포함된 응답을 반환한다.</p>
     */
    @GetMapping("/health-data/{userId}")
    public ResponseEntity<Map<String, Object>> getHealthData(@PathVariable String userId) {
        log.debug("내부 건강 데이터 조회: userId={}", userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("medicalData",    healthDataStore.getMedicalData(userId).orElse(null));
        result.put("checkupData",    healthDataStore.getCheckupData(userId).orElse(null));
        result.put("hasMedicalData", healthDataStore.hasMedicalData(userId));
        result.put("hasCheckupData", healthDataStore.hasCheckupData(userId));

        return ResponseEntity.ok(result);
    }
}
