package com.medicatch.insurance.service;

import com.medicatch.insurance.client.HealthServiceClient;
import com.medicatch.insurance.config.OpenAiProperties;
import com.medicatch.insurance.dto.response.contract.ActualLossContract;
import com.medicatch.insurance.dto.response.contract.FlatRateContract;
import com.medicatch.insurance.dto.response.contract.InsuranceContractResponse;
import com.medicatch.insurance.dto.response.recommendation.CoverageGap;
import com.medicatch.insurance.dto.response.recommendation.InsuranceRecommendation;
import com.medicatch.insurance.store.InsuranceDataStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 보험 추천 분석 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceRecommendationService {

    private static final String GAP_DENTAL        = "DENTAL";
    private static final String GAP_MENTAL_HEALTH = "MENTAL_HEALTH";
    private static final String GAP_ORIENTAL      = "ORIENTAL_MEDICINE";
    private static final String GAP_OPHTHALMOLOGY = "OPHTHALMOLOGY";
    private static final String GAP_DERMATOLOGY   = "DERMATOLOGY";

    private final HealthServiceClient healthServiceClient;
    private final InsuranceDataStore  insuranceDataStore;
    private final OpenAiProperties    openAiProperties;
    private final WebClient           openAiWebClient;

    public InsuranceRecommendation recommend(String userId) {
        log.info("보험 추천 분석 시작: userId={}", userId);

        List<Map<String, Object>> treatList = fetchTreatList(userId);
        boolean healthAvailable = !treatList.isEmpty();

        InsuranceContractResponse contractData = insuranceDataStore.getContractData(userId).orElse(null);
        boolean insuranceAvailable = contractData != null;

        // 1. 보험 상품명 기반으로 이미 보장받고 있는 유형 추출
        Set<String> coveredTypes = extractCoveredTypes(contractData);

        // 2. 진료 기록과 비교하여 공백 감지
        List<CoverageGap> gaps = detectCoverageGaps(treatList, coveredTypes);

        List<String> recommendedTypes = gaps.stream()
                .map(CoverageGap::getRecommendedInsuranceType)
                .distinct()
                .collect(Collectors.toList());

        int contractCount = countContracts(contractData);
        String aiMessage = generateAiMessage(userId, treatList, contractData, gaps, healthAvailable, insuranceAvailable);

        return InsuranceRecommendation.builder()
                .userId(userId)
                .generatedAt(LocalDateTime.now())
                .coverageGaps(gaps)
                .recommendedTypes(recommendedTypes)
                .aiMessage(aiMessage)
                .analyzedTreatmentCount(treatList.size())
                .currentContractCount(contractCount)
                .healthDataAvailable(healthAvailable)
                .insuranceDataAvailable(insuranceAvailable)
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchTreatList(String userId) {
        try {
            Map<String, Object> healthData = healthServiceClient.getHealthData(userId);
            Object medicalData = healthData.get("medicalData");
            if (!(medicalData instanceof Map)) return List.of();

            Object rawList = ((Map<?, ?>) medicalData).get("resBasicTreatList");
            if (rawList instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map) {
                return (List<Map<String, Object>>) rawList;
            }
        } catch (Exception e) {
            log.warn("진료 기록 조회 실패: userId={}, error={}", userId, e.getMessage());
        }
        return List.of();
    }

    private List<CoverageGap> detectCoverageGaps(List<Map<String, Object>> treats, Set<String> coveredTypes) {
        Map<String, List<Map<String, Object>>> byDept = treats.stream()
                .filter(t -> t.get("resDeptCdNm") instanceof String s && !s.isBlank())
                .collect(Collectors.groupingBy(t -> (String) t.get("resDeptCdNm")));

        return byDept.entrySet().stream()
                .map(entry -> {
                    String dept = entry.getKey();
                    String gapType = detectGapType(dept);

                    // 이미 해당 유형의 보험이 있다면 공백에서 제외
                    if (gapType == null || coveredTypes.contains(gapType)) return null;

                    long totalPatPayment = entry.getValue().stream()
                            .mapToLong(t -> parseLong((String) t.get("resPatPayment")))
                            .sum();

                    return CoverageGap.builder()
                            .department(dept)
                            .visitCount(entry.getValue().size())
                            .totalPatientPayment(totalPatPayment)
                            .gapType(gapType)
                            .description(buildDescription(dept, gapType))
                            .recommendedInsuranceType(buildRecommendedType(gapType))
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(CoverageGap::getTotalPatientPayment).reversed())
                .collect(Collectors.toList());
    }

    private String detectGapType(String dept) {
        if (dept.contains("치과") || dept.contains("구강") || dept.contains("치아")) return GAP_DENTAL;
        if (dept.contains("정신") || dept.contains("신경"))                      return GAP_MENTAL_HEALTH;
        if (dept.contains("한방") || dept.contains("한의"))                      return GAP_ORIENTAL;
        if (dept.contains("안과"))                                               return GAP_OPHTHALMOLOGY;
        if (dept.contains("피부"))                                               return GAP_DERMATOLOGY;
        return null;
    }

    // ── 보험 상품명 기반 보장 유형 추출 (수정된 필드명 반영) ────────────────

    private Set<String> extractCoveredTypes(InsuranceContractResponse contractData) {
        Set<String> covered = new HashSet<>();
        if (contractData == null) return covered;

        // 1. 정액형 보험 상품명 확인 (resProductNm -> resInsuranceName)
        if (contractData.getResFlatRateContractList() != null) {
            contractData.getResFlatRateContractList().stream()
                    .map(FlatRateContract::getResInsuranceName) // 수정됨
                    .filter(Objects::nonNull)
                    .forEach(nm -> checkKeywords(nm, covered));
        }

        // 2. 실손형 보험 상품명 확인 (resProductNm -> resInsuranceName)
        if (contractData.getResActualLossContractList() != null) {
            contractData.getResActualLossContractList().stream()
                    .map(ActualLossContract::getResInsuranceName) // 수정됨
                    .filter(Objects::nonNull)
                    .forEach(nm -> checkKeywords(nm, covered));
        }

        return covered;
    }

    private void checkKeywords(String name, Set<String> covered) {
        if (name.contains("치아") || name.contains("치과")) covered.add(GAP_DENTAL);
        if (name.contains("정신") || name.contains("신경")) covered.add(GAP_MENTAL_HEALTH);
        if (name.contains("한방") || name.contains("한의")) covered.add(GAP_ORIENTAL);
        if (name.contains("안과"))                        covered.add(GAP_OPHTHALMOLOGY);
        if (name.contains("피부"))                        covered.add(GAP_DERMATOLOGY);
    }

    // ── GPT-4o 추천 메시지 생성 ───────────────────────────────────────────

    private String generateAiMessage(
            String userId,
            List<Map<String, Object>> treats,
            InsuranceContractResponse contractData,
            List<CoverageGap> gaps,
            boolean healthAvailable,
            boolean insuranceAvailable) {

        if (openAiProperties.getApiKey() == null || openAiProperties.getApiKey().isBlank()) {
            return buildFallbackMessage(gaps, insuranceAvailable);
        }

        String context = buildGptContext(userId, treats, contractData, gaps, healthAvailable, insuranceAvailable);

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", openAiProperties.getModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", context)
                    )
            );

            Map<String, Object> response = openAiWebClient.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return extractContent(response);
        } catch (Exception e) {
            log.error("GPT 호출 실패: {}", e.getMessage());
            return buildFallbackMessage(gaps, insuranceAvailable);
        }
    }

    private String buildGptContext(
            String userId,
            List<Map<String, Object>> treats,
            InsuranceContractResponse contractData,
            List<CoverageGap> gaps,
            boolean healthAvailable,
            boolean insuranceAvailable) {

        StringBuilder sb = new StringBuilder();
        if (healthAvailable) {
            sb.append("- 진료 건수: ").append(treats.size()).append("건\n");
        }
        if (insuranceAvailable && contractData != null) {
            int total = countContracts(contractData);
            sb.append("- 가입 보험: ").append(total).append("건\n");
            // 상품명 포함하여 GPT에게 전달
            if (contractData.getResFlatRateContractList() != null) {
                contractData.getResFlatRateContractList().forEach(c ->
                        sb.append("  [정액] ").append(c.getResInsuranceName()).append("\n"));
            }
        }
        sb.append("- 감지된 공백: ");
        gaps.forEach(g -> sb.append(g.getDepartment()).append("(").append(g.getVisitCount()).append("회), "));

        return sb.toString();
    }

    private static final String SYSTEM_PROMPT = "당신은 보험 전문 AI Medicatch입니다. 진료 기록과 보험 현황을 분석해 300자 이내로 따뜻하게 보험을 추천해 주세요. 수치를 활용하면 좋습니다.";

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        try {
            var choices = (List<Map<String, Object>>) response.get("choices");
            var message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            return "분석 결과를 생성하는 중 오류가 발생했습니다.";
        }
    }

    private String buildFallbackMessage(List<CoverageGap> gaps, boolean insuranceAvailable) {
        if (gaps.isEmpty()) return "현재 분석된 보장 공백이 없습니다. 건강 상태를 꾸준히 관리해 보세요!";
        return "최근 진료 이력을 바탕으로 " + gaps.get(0).getRecommendedInsuranceType() + " 가입을 고려해 보시는 것은 어떨까요?";
    }

    private int countContracts(InsuranceContractResponse c) {
        if (c == null) return 0;
        int cnt = 0;
        if (c.getResFlatRateContractList()   != null) cnt += c.getResFlatRateContractList().size();
        if (c.getResActualLossContractList() != null) cnt += c.getResActualLossContractList().size();
        return cnt;
    }

    private long parseLong(String value) {
        if (value == null || value.isBlank()) return 0L;
        return Long.parseLong(value.replaceAll("[^0-9]", ""));
    }

    private String buildDescription(String dept, String gapType) {
        return dept + " 진료 이력이 빈번하나 관련 보장이 부족합니다.";
    }

    private String buildRecommendedType(String gapType) {
        return switch (gapType) {
            case GAP_DENTAL        -> "치아보험";
            case GAP_MENTAL_HEALTH -> "마음건강 특약";
            case GAP_ORIENTAL      -> "한방 실손특약";
            case GAP_OPHTHALMOLOGY -> "안과질환 보험";
            case GAP_DERMATOLOGY   -> "피부질환 특약";
            default                -> "종합 건강보험";
        };
    }
}