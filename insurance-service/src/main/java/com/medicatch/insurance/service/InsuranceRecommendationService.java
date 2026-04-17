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
import java.util.stream.Stream;

/**
 * 보험 추천 분석 서비스.
 *
 * <h3>분석 순서</h3>
 * <ol>
 *   <li>health-service Feign으로 진료 기록 로드</li>
 *   <li>InsuranceDataStore에서 보험 계약 정보 로드</li>
 *   <li>진료과별 방문 횟수 집계 → 보장 공백 감지</li>
 *   <li>보험 상품명에서 이미 보장된 유형 추출 → 공백 필터링</li>
 *   <li>GPT-4o에 컨텍스트 전달 → 맞춤 추천 메시지 생성</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceRecommendationService {

    // 보장 공백 유형 상수
    private static final String GAP_DENTAL        = "DENTAL";
    private static final String GAP_MENTAL_HEALTH = "MENTAL_HEALTH";
    private static final String GAP_ORIENTAL      = "ORIENTAL_MEDICINE";
    private static final String GAP_OPHTHALMOLOGY = "OPHTHALMOLOGY";
    private static final String GAP_DERMATOLOGY   = "DERMATOLOGY";

    private final HealthServiceClient healthServiceClient;
    private final InsuranceDataStore  insuranceDataStore;
    private final OpenAiProperties    openAiProperties;
    private final WebClient           openAiWebClient;

    // ── 공개 API ─────────────────────────────────────────────────────────

    public InsuranceRecommendation recommend(String userId) {
        log.info("보험 추천 분석 시작: userId={}", userId);

        // 1. 진료 기록 로드
        List<Map<String, Object>> treatList = fetchTreatList(userId);
        boolean healthAvailable = !treatList.isEmpty();
        log.info("진료 기록 로드 완료: {}건", treatList.size());

        // 2. 보험 계약 정보 로드
        InsuranceContractResponse contractData = insuranceDataStore.getContractData(userId).orElse(null);
        boolean insuranceAvailable = contractData != null;

        // 3. 보장 공백 감지
        Set<String> coveredTypes = extractCoveredTypes(contractData);
        List<CoverageGap> gaps = detectCoverageGaps(treatList, coveredTypes);
        log.info("보장 공백 감지: {}건", gaps.size());

        // 4. 추천 보험 유형 목록
        List<String> recommendedTypes = gaps.stream()
                .map(CoverageGap::getRecommendedInsuranceType)
                .distinct()
                .collect(Collectors.toList());

        // 5. 계약 건수
        int contractCount = countContracts(contractData);

        // 6. GPT-4o 추천 메시지 생성
        String aiMessage = generateAiMessage(userId, treatList, contractData, gaps, healthAvailable, insuranceAvailable);

        log.info("보험 추천 완료: userId={}, gaps={}, aiMessage={}", userId, gaps.size(), aiMessage.length());

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

    // ── 진료 기록 로드 ────────────────────────────────────────────────────

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
            log.warn("health-service 진료 기록 조회 실패: userId={}, error={}", userId, e.getMessage());
        }
        return List.of();
    }

    // ── 보장 공백 감지 ────────────────────────────────────────────────────

    private List<CoverageGap> detectCoverageGaps(List<Map<String, Object>> treats, Set<String> coveredTypes) {
        // 진료과별 집계
        Map<String, List<Map<String, Object>>> byDept = treats.stream()
                .filter(t -> t.get("resDeptCdNm") instanceof String s && !s.isBlank())
                .collect(Collectors.groupingBy(t -> (String) t.get("resDeptCdNm")));

        return byDept.entrySet().stream()
                .map(entry -> {
                    String dept = entry.getKey();
                    String gapType = detectGapType(dept);
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
        if (dept.contains("정신") || dept.contains("신경정신"))                      return GAP_MENTAL_HEALTH;
        if (dept.contains("한방") || dept.contains("한의"))                          return GAP_ORIENTAL;
        if (dept.contains("안과"))                                                    return GAP_OPHTHALMOLOGY;
        if (dept.contains("피부"))                                                    return GAP_DERMATOLOGY;
        return null;
    }

    // ── 보험 상품명 기반 보장 유형 추출 ──────────────────────────────────

    private Set<String> extractCoveredTypes(InsuranceContractResponse contractData) {
        Set<String> covered = new HashSet<>();
        if (contractData == null) return covered;

        Stream<String> flatNames = safeProductNames(contractData.getResFlatRateContractList(),
                FlatRateContract::getResProductNm);
        Stream<String> actualNames = safeProductNamesActual(contractData.getResActualLossContractList());

        Stream.concat(flatNames, actualNames).forEach(nm -> {
            if (nm.contains("치아") || nm.contains("치과")) covered.add(GAP_DENTAL);
            if (nm.contains("정신") || nm.contains("신경")) covered.add(GAP_MENTAL_HEALTH);
            if (nm.contains("한방") || nm.contains("한의")) covered.add(GAP_ORIENTAL);
            if (nm.contains("안과"))                        covered.add(GAP_OPHTHALMOLOGY);
            if (nm.contains("피부"))                        covered.add(GAP_DERMATOLOGY);
        });

        // 실손 특약(resSpecialClause)도 확인
        if (contractData.getResActualLossContractList() != null) {
            contractData.getResActualLossContractList().stream()
                    .map(ActualLossContract::getResSpecialClause)
                    .filter(Objects::nonNull)
                    .forEach(clause -> {
                        if (clause.contains("치아") || clause.contains("치과")) covered.add(GAP_DENTAL);
                        if (clause.contains("정신"))                            covered.add(GAP_MENTAL_HEALTH);
                        if (clause.contains("한방") || clause.contains("한의")) covered.add(GAP_ORIENTAL);
                        if (clause.contains("안과"))                            covered.add(GAP_OPHTHALMOLOGY);
                        if (clause.contains("피부"))                            covered.add(GAP_DERMATOLOGY);
                    });
        }

        return covered;
    }

    @FunctionalInterface
    private interface NameExtractor<T> {
        String extract(T item);
    }

    private <T> Stream<String> safeProductNames(List<T> list, NameExtractor<T> extractor) {
        if (list == null) return Stream.empty();
        return list.stream().map(extractor::extract).filter(Objects::nonNull);
    }

    private Stream<String> safeProductNamesActual(List<ActualLossContract> list) {
        if (list == null) return Stream.empty();
        return list.stream().map(ActualLossContract::getResProductNm).filter(Objects::nonNull);
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
            log.warn("OpenAI API Key 미설정 — 기본 추천 메시지 사용");
            return buildFallbackMessage(gaps, insuranceAvailable);
        }

        String context = buildGptContext(userId, treats, contractData, gaps, healthAvailable, insuranceAvailable);

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", openAiProperties.getModel(),
                    "max_tokens", openAiProperties.getMaxTokens(),
                    "temperature", openAiProperties.getTemperature(),
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", context)
                    )
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = openAiWebClient.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return extractContent(response);

        } catch (Exception e) {
            log.error("GPT-4o 호출 실패: {}", e.getMessage());
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
        sb.append("=== 사용자 정보 ===\n");
        sb.append("사용자 ID: ").append(userId).append("\n\n");

        if (healthAvailable) {
            sb.append("=== 진료 기록 요약 ===\n");
            sb.append("총 방문 건수: ").append(treats.size()).append("건\n");

            Map<String, Long> deptCount = treats.stream()
                    .filter(t -> t.get("resDeptCdNm") instanceof String s && !s.isBlank())
                    .collect(Collectors.groupingBy(t -> (String) t.get("resDeptCdNm"), Collectors.counting()));

            sb.append("주요 진료과:\n");
            deptCount.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(e -> sb.append("  - ").append(e.getKey()).append(": ").append(e.getValue()).append("회\n"));
            sb.append("\n");
        } else {
            sb.append("=== 진료 기록 ===\n조회된 진료 기록이 없습니다.\n\n");
        }

        if (insuranceAvailable && contractData != null) {
            sb.append("=== 현재 보험 계약 현황 ===\n");
            int flatCount   = contractData.getResFlatRateContractList()   != null ? contractData.getResFlatRateContractList().size()   : 0;
            int actualCount = contractData.getResActualLossContractList() != null ? contractData.getResActualLossContractList().size() : 0;
            sb.append("정액보험: ").append(flatCount).append("건\n");
            sb.append("실손보험: ").append(actualCount).append("건\n");

            if (contractData.getResActualLossContractList() != null) {
                contractData.getResActualLossContractList().stream().limit(3).forEach(c ->
                        sb.append("  실손: ").append(c.getResProductNm()).append(" (").append(c.getResCompanyNm()).append(")\n")
                );
            }
            if (contractData.getResFlatRateContractList() != null) {
                contractData.getResFlatRateContractList().stream().limit(3).forEach(c ->
                        sb.append("  정액: ").append(c.getResProductNm()).append(" (").append(c.getResCompanyNm()).append(")\n")
                );
            }
            sb.append("\n");
        } else {
            sb.append("=== 보험 계약 ===\n조회된 보험 계약이 없거나 데이터를 불러오지 못했습니다.\n\n");
        }

        sb.append("=== 감지된 보장 공백 ===\n");
        if (gaps.isEmpty()) {
            sb.append("현재 진료 기록 기준 보장 공백이 없습니다.\n");
        } else {
            gaps.forEach(g ->
                sb.append("  - ").append(g.getDepartment())
                        .append(": ").append(g.getVisitCount()).append("회 방문")
                        .append(", 본인부담금 ").append(g.getTotalPatientPayment()).append("원")
                        .append(" [").append(g.getGapType()).append("]\n")
            );
        }
        sb.append("\n");
        sb.append("위 데이터를 바탕으로 맞춤 보험 추천 메시지를 작성해주세요.");

        return sb.toString();
    }

    private static final String SYSTEM_PROMPT =
            """
            당신은 헬스케어 & 보험 플랫폼 Medicatch의 보험 전문 AI 어시스턴트입니다.
            사용자의 실제 진료 기록과 현재 가입된 보험 계약을 분석하여
            보장 공백을 파악하고 맞춤형 보험 추천 메시지를 제공합니다.

            작성 지침:
            1. 300자 이내의 간결하고 친근한 한국어로 작성하세요.
            2. 구체적인 수치(방문 횟수, 본인부담금)를 활용하여 설득력 있게 작성하세요.
            3. 감지된 보장 공백에 대해 적합한 보험 상품 유형을 1~3가지 추천하세요.
            4. 특정 보험사나 상품명을 언급하지 말고, 상품 유형(치아보험, 실손보험 등)만 언급하세요.
            5. 의료 진단이나 구체적인 의학적 조언은 하지 마세요.
            6. 데이터가 부족할 경우 일반적인 조언을 제공하세요.
            """;

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        if (response == null) return buildFallbackMessage(List.of(), false);
        var choices = (List<?>) response.get("choices");
        if (choices == null || choices.isEmpty()) return buildFallbackMessage(List.of(), false);
        var first   = (Map<String, Object>) choices.get(0);
        var message = (Map<String, Object>) first.get("message");
        if (message == null) return buildFallbackMessage(List.of(), false);
        return String.valueOf(message.get("content"));
    }

    // ── 폴백 메시지 ──────────────────────────────────────────────────────

    private String buildFallbackMessage(List<CoverageGap> gaps, boolean insuranceAvailable) {
        if (gaps.isEmpty()) {
            if (!insuranceAvailable) {
                return "보험 계약 정보를 먼저 조회해주세요. 조회 후 진료 기록과 비교하여 보장 공백을 분석해드립니다.";
            }
            return "현재 진료 기록 기준으로 보장 공백이 발견되지 않았습니다. 앞으로도 정기적으로 확인하세요.";
        }

        StringBuilder msg = new StringBuilder("진료 기록 분석 결과, ");
        msg.append(gaps.size()).append("개의 보장 공백이 발견되었습니다. ");
        gaps.stream().limit(2).forEach(g ->
                msg.append(g.getDepartment()).append(" 진료(").append(g.getVisitCount()).append("회)에 대한 ")
                        .append(g.getRecommendedInsuranceType()).append(" 가입을 검토해보세요. ")
        );
        return msg.toString().trim();
    }

    // ── 유틸 ─────────────────────────────────────────────────────────────

    private int countContracts(InsuranceContractResponse c) {
        if (c == null) return 0;
        int cnt = 0;
        if (c.getResFlatRateContractList()   != null) cnt += c.getResFlatRateContractList().size();
        if (c.getResActualLossContractList() != null) cnt += c.getResActualLossContractList().size();
        return cnt;
    }

    private long parseLong(String value) {
        if (value == null || value.isBlank()) return 0L;
        try {
            return Long.parseLong(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String buildDescription(String dept, String gapType) {
        return switch (gapType) {
            case GAP_DENTAL        -> dept + " 진료 이력이 있으나 치아(치과)보험 미가입";
            case GAP_MENTAL_HEALTH -> dept + " 진료 이력이 있으나 정신건강 관련 보험 미가입";
            case GAP_ORIENTAL      -> dept + " 진료 이력이 있으나 한방 특약 미가입";
            case GAP_OPHTHALMOLOGY -> dept + " 진료 이력이 있으나 안과 특약 미가입";
            case GAP_DERMATOLOGY   -> dept + " 진료 이력이 있으나 피부 관련 보험 미가입";
            default                -> dept + " 진료 이력이 있으나 관련 보험 미가입";
        };
    }

    private String buildRecommendedType(String gapType) {
        return switch (gapType) {
            case GAP_DENTAL        -> "치아보험(치과보험)";
            case GAP_MENTAL_HEALTH -> "정신건강 특약 실손보험";
            case GAP_ORIENTAL      -> "한방 특약 실손보험";
            case GAP_OPHTHALMOLOGY -> "안과·시력교정 특약 보험";
            case GAP_DERMATOLOGY   -> "피부 특약 보험";
            default                -> "해당 진료 영역 보험";
        };
    }
}
