package com.medicatch.insurance.store;

import com.medicatch.insurance.dto.response.contract.InsuranceContractResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 유저별 최근 보험 계약정보 인메모리 캐시.
 *
 * <p>CODEF 계약정보 조회 성공 후 userId 키로 보관한다.
 * health-service가 /internal/contract-data/{userId} 로 조회할 때 사용.</p>
 */
@Component
public class InsuranceDataStore {

    private final ConcurrentHashMap<String, InsuranceContractResponse> contractCache =
            new ConcurrentHashMap<>();

    public void storeContractData(String userId, InsuranceContractResponse data) {
        if (userId != null && !userId.isBlank() && data != null) {
            contractCache.put(userId, data);
        }
    }

    public Optional<InsuranceContractResponse> getContractData(String userId) {
        return Optional.ofNullable(contractCache.get(userId));
    }

    public boolean hasContractData(String userId) {
        return contractCache.containsKey(userId);
    }
}
