package com.medicatch.health.store;

import com.medicatch.health.dto.response.medical.MedicalInfoResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 유저별 최근 건강 데이터 인메모리 캐시.
 *
 * <p>CODEF 2-Way 인증 성공 후 결과를 userId 키로 보관한다.
 * chat-service가 /internal/health-data/{userId} 로 조회할 때 사용.</p>
 */
@Component
public class HealthDataStore {

    private final ConcurrentHashMap<String, MedicalInfoResponse> medicalCache  = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<String, Object>> checkupCache  = new ConcurrentHashMap<>();

    public void storeMedicalData(String userId, MedicalInfoResponse data) {
        if (userId != null && !userId.isBlank() && data != null) {
            medicalCache.put(userId, data);
        }
    }

    public void storeCheckupData(String userId, Map<String, Object> data) {
        if (userId != null && !userId.isBlank() && data != null) {
            checkupCache.put(userId, data);
        }
    }

    public Optional<MedicalInfoResponse> getMedicalData(String userId) {
        return Optional.ofNullable(medicalCache.get(userId));
    }

    public Optional<Map<String, Object>> getCheckupData(String userId) {
        return Optional.ofNullable(checkupCache.get(userId));
    }

    public boolean hasMedicalData(String userId) {
        return medicalCache.containsKey(userId);
    }

    public boolean hasCheckupData(String userId) {
        return checkupCache.containsKey(userId);
    }
}
