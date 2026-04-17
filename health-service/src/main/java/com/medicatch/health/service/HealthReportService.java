package com.medicatch.health.service;

import com.medicatch.health.client.InsuranceServiceClient;
import com.medicatch.health.dto.response.medical.BasicTreat;
import com.medicatch.health.dto.response.medical.MedicalInfoResponse;
import com.medicatch.health.dto.response.report.CoverageGapItem;
import com.medicatch.health.dto.response.report.HealthReportResponse;
import com.medicatch.health.dto.response.report.InsuranceClaimableItem;
import com.medicatch.health.store.HealthDataStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 건강 통합 리포트 분석 서비스.
 *
 * <h3>분석 순서</h3>
 * <ol>
 *   <li>HealthDataStore에서 진료기록 로드</li>
 *   <li>InsuranceServiceClient(Feign)로 보험 계약정보 로드</li>
 *   <li>최근 N개월 진료기록 필터링</li>
 *   <li>월별 방문 횟수, 진료과별 현황, 비용 집계</li>
 *   <li>보험 계약 유무 기반 청구 가능 항목 및 보장 공백 도출</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthReportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 보장 공백 분류 키
    private static final String GAP_DENTAL           = "DENTAL";
    private static final String GAP_MENTAL_HEALTH    = "MENTAL_HEALTH";
    private static final String GAP_ORIENTAL         = "ORIENTAL_MEDICINE";
    private static final String GAP_OPHTHALMOLOGY    = "OPHTHALMOLOGY";
    private static final String GAP_DERMATOLOGY      = "DERMATOLOGY";

    private final HealthDataStore        healthDataStore;
    private final InsuranceServiceClient insuranceServiceClient;

    // ── 공개 API ──────────────────────────────────────────────────────────

    /**
     * 건강 통합 리포트 생성.
     *
     * @param userId 대상 사용자 ID (X-User-Id 헤더 값)
     * @param months 분석 기간 (개월, 기본 12)
     */
    public HealthReportResponse generateReport(String userId, int months) {
        log.info("건강 통합 리포트 생성 시작: userId={}, months={}", userId, months);

        // 1. 진료기록 로드
        MedicalInfoResponse medicalData = healthDataStore.getMedicalData(userId).orElse(null);
        if (medicalData == null) {
            log.warn("진료기록 없음: userId={}", userId);
        }

        // 2. 보험 계약정보 로드 (실패 허용)
        Map<String, Object> insuranceDataMap = null;
        boolean insuranceAvailable = false;
        try {
            insuranceDataMap = insuranceServiceClient.getContractData(userId);
            insuranceAvailable = Boolean.TRUE.equals(insuranceDataMap.get("hasContractData"));
            log.debug("보험 데이터 조회 성공: userId={}, hasData={}", userId, insuranceAvailable);
        } catch (Exception e) {
            log.warn("보험 데이터 조회 실패 (리포트 계속): userId={}, error={}", userId, e.getMessage());
        }

        // 3. 분석 기간 필터링
        LocalDate cutoff = LocalDate.now().minusMonths(months);
        List<BasicTreat> recentTreats = filterByDate(medicalData, cutoff);
        log.info("분석 대상 진료 건수: {}건 ({}개월)", recentTreats.size(), months);

        // 4. 통계 집계
        Map<String, Integer> monthlyVisitCount = analyzeMonthlyVisits(recentTreats);
        Map<String, Integer> departmentUsage   = analyzeDepartmentUsage(recentTreats);
        long totalMedicalCost  = sumCost(recentTreats, BasicTreat::getResTotalCost);
        long totalPatientCost  = sumCost(recentTreats, BasicTreat::getResPatPayment);

        // 5. 보험 교차 분석
        List<InsuranceClaimableItem> claimableItems = findClaimableItems(recentTreats, insuranceDataMap);
        List<CoverageGapItem>        coverageGaps   = findCoverageGaps(recentTreats, insuranceDataMap);

        log.info("리포트 생성 완료: userId={}, claimable={}건, gaps={}건",
                userId, claimableItems.size(), coverageGaps.size());

        return HealthReportResponse.builder()
                .userId(userId)
                .analyzedMonths(months)
                .generatedAt(java.time.LocalDateTime.now())
                .monthlyVisitCount(monthlyVisitCount)
                .departmentUsage(departmentUsage)
                .totalMedicalCost(totalMedicalCost)
                .totalPatientCost(totalPatientCost)
                .totalVisitCount(recentTreats.size())
                .insuranceClaimable(claimableItems)
                .coverageGap(coverageGaps)
                .insuranceDataAvailable(insuranceAvailable)
                .build();
    }

    // ── 진료기록 필터 & 통계 ────────────────────────────────────────────

    private List<BasicTreat> filterByDate(MedicalInfoResponse data, LocalDate cutoff) {
        if (data == null || data.getResBasicTreatList() == null) return List.of();
        return data.getResBasicTreatList().stream()
                .filter(t -> {
                    if (t.getReqDate() == null || t.getReqDate().length() < 8) return false;
                    try {
                        return !LocalDate.parse(t.getReqDate(), DATE_FORMATTER).isBefore(cutoff);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    /** 월별 방문 횟수 — "yyyy-MM" 키, TreeMap으로 시간순 정렬 */
    private Map<String, Integer> analyzeMonthlyVisits(List<BasicTreat> treats) {
        return treats.stream()
                .filter(t -> t.getReqDate() != null && t.getReqDate().length() >= 6)
                .collect(Collectors.groupingBy(
                        t -> t.getReqDate().substring(0, 4) + "-" + t.getReqDate().substring(4, 6),
                        TreeMap::new,
                        Collectors.summingInt(t -> 1)
                ));
    }

    /** 진료과별 이용 현황 — 방문 횟수 내림차순 */
    private Map<String, Integer> analyzeDepartmentUsage(List<BasicTreat> treats) {
        return treats.stream()
                .filter(t -> t.getResDeptCdNm() != null && !t.getResDeptCdNm().isBlank())
                .collect(Collectors.groupingBy(BasicTreat::getResDeptCdNm, Collectors.summingInt(t -> 1)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new
                ));
    }

    @FunctionalInterface
    private interface CostExtractor {
        String extract(BasicTreat t);
    }

    private long sumCost(List<BasicTreat> treats, CostExtractor extractor) {
        return treats.stream()
                .mapToLong(t -> parseLong(extractor.extract(t)))
                .sum();
    }

    // ── 보험 청구 가능 항목 ─────────────────────────────────────────────

    private List<InsuranceClaimableItem> findClaimableItems(
            List<BasicTreat> treats, Map<String, Object> insuranceDataMap) {

        boolean hasActualLoss = hasActualLossContract(insuranceDataMap);
        boolean hasFlatRate   = hasFlatRateContract(insuranceDataMap);

        if (!hasActualLoss && !hasFlatRate) return List.of();

        String contractType = hasActualLoss ? "실손" : "정액";
        String reason = hasActualLoss
                ? "실손보험 적용 가능 — 본인부담금 청구 대상"
                : "정액보험 적용 검토 필요 — 담당 보험사에 문의하세요";

        return treats.stream()
                .filter(t -> parseLong(t.getResPatPayment()) > 0)
                .map(t -> InsuranceClaimableItem.builder()
                        .treatmentDate(formatDate(t.getReqDate()))
                        .hospitalName(t.getResMedInstNm())
                        .department(t.getResDeptCdNm())
                        .diseaseCode(t.getResDissCd())
                        .diseaseCodeName(t.getResDissCdNm())
                        .patientPayment(parseLong(t.getResPatPayment()))
                        .claimableReason(reason)
                        .contractType(contractType)
                        .build())
                .sorted(Comparator.comparingLong(InsuranceClaimableItem::getPatientPayment).reversed())
                .collect(Collectors.toList());
    }

    // ── 보장 공백 분석 ──────────────────────────────────────────────────

    private List<CoverageGapItem> findCoverageGaps(
            List<BasicTreat> treats, Map<String, Object> insuranceDataMap) {

        Set<String> coveredTypes = extractCoveredGapTypes(insuranceDataMap);

        Map<String, List<BasicTreat>> byDept = treats.stream()
                .filter(t -> t.getResDeptCdNm() != null && !t.getResDeptCdNm().isBlank())
                .collect(Collectors.groupingBy(BasicTreat::getResDeptCdNm));

        return byDept.entrySet().stream()
                .map(entry -> {
                    String dept       = entry.getKey();
                    List<BasicTreat> deptTreats = entry.getValue();
                    String gapType    = detectGapType(dept);

                    if (gapType == null || coveredTypes.contains(gapType)) return null;

                    long totalCost       = deptTreats.stream().mapToLong(t -> parseLong(t.getResTotalCost())).sum();
                    long totalPatPayment = deptTreats.stream().mapToLong(t -> parseLong(t.getResPatPayment())).sum();

                    return CoverageGapItem.builder()
                            .department(dept)
                            .visitCount(deptTreats.size())
                            .totalCost(totalCost)
                            .totalPatientPayment(totalPatPayment)
                            .gapType(gapType)
                            .gapDescription(buildGapDescription(dept, gapType))
                            .recommendation(buildRecommendation(gapType))
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(CoverageGapItem::getTotalPatientPayment).reversed())
                .collect(Collectors.toList());
    }

    /** 진료과명에서 보장 공백 유형 감지 (해당 없으면 null) */
    private String detectGapType(String dept) {
        if (dept.contains("치과") || dept.contains("구강") || dept.contains("치아")) return GAP_DENTAL;
        if (dept.contains("정신") || dept.contains("신경정신"))                      return GAP_MENTAL_HEALTH;
        if (dept.contains("한방") || dept.contains("한의"))                          return GAP_ORIENTAL;
        if (dept.contains("안과"))                                                    return GAP_OPHTHALMOLOGY;
        if (dept.contains("피부"))                                                    return GAP_DERMATOLOGY;
        return null; // 일반 실손으로 커버되는 진료과
    }

    /** 보험 계약 상품명에서 보장 유형 Set 추출 */
    @SuppressWarnings("unchecked")
    private Set<String> extractCoveredGapTypes(Map<String, Object> insuranceDataMap) {
        Set<String> covered = new HashSet<>();
        if (insuranceDataMap == null) return covered;

        Object rawContract = insuranceDataMap.get("contractData");
        if (!(rawContract instanceof Map)) return covered;

        Map<String, Object> contractData = (Map<String, Object>) rawContract;

        Stream.concat(
                asList(contractData.get("resFlatRateContractList")).stream(),
                asList(contractData.get("resActualLossContractList")).stream()
        ).forEach(contract -> {
            String nm = (String) contract.get("resProductNm");
            if (nm == null) return;
            if (nm.contains("치아") || nm.contains("치과"))     covered.add(GAP_DENTAL);
            if (nm.contains("정신") || nm.contains("신경"))     covered.add(GAP_MENTAL_HEALTH);
            if (nm.contains("한방") || nm.contains("한의"))     covered.add(GAP_ORIENTAL);
            if (nm.contains("안과"))                            covered.add(GAP_OPHTHALMOLOGY);
            if (nm.contains("피부"))                            covered.add(GAP_DERMATOLOGY);
        });

        return covered;
    }

    @SuppressWarnings("unchecked")
    private boolean hasActualLossContract(Map<String, Object> insuranceDataMap) {
        if (insuranceDataMap == null) return false;
        Object raw = insuranceDataMap.get("contractData");
        if (!(raw instanceof Map)) return false;
        List<?> list = (List<?>) ((Map<?, ?>) raw).get("resActualLossContractList");
        return list != null && !list.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private boolean hasFlatRateContract(Map<String, Object> insuranceDataMap) {
        if (insuranceDataMap == null) return false;
        Object raw = insuranceDataMap.get("contractData");
        if (!(raw instanceof Map)) return false;
        List<?> list = (List<?>) ((Map<?, ?>) raw).get("resFlatRateContractList");
        return list != null && !list.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asList(Object obj) {
        if (obj instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map) {
            return (List<Map<String, Object>>) obj;
        }
        return List.of();
    }

    // ── 유틸 ────────────────────────────────────────────────────────────

    private long parseLong(String value) {
        if (value == null || value.isBlank()) return 0L;
        try {
            return Long.parseLong(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.length() < 8) return dateStr;
        return dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8);
    }

    private String buildGapDescription(String dept, String gapType) {
        return switch (gapType) {
            case GAP_DENTAL        -> dept + " 진료 이력이 있으나 치아(치과)보험 미가입";
            case GAP_MENTAL_HEALTH -> dept + " 진료 이력이 있으나 정신건강 관련 보험 미가입";
            case GAP_ORIENTAL      -> dept + " 진료 이력이 있으나 한방 특약 미가입";
            case GAP_OPHTHALMOLOGY -> dept + " 진료 이력이 있으나 안과 특약 미가입";
            case GAP_DERMATOLOGY   -> dept + " 진료 이력이 있으나 피부 관련 보험 미가입";
            default                -> dept + " 진료 이력이 있으나 관련 보험 미가입";
        };
    }

    private String buildRecommendation(String gapType) {
        return switch (gapType) {
            case GAP_DENTAL        -> "치아보험(치과보험) 가입을 검토해보세요";
            case GAP_MENTAL_HEALTH -> "정신건강 특약이 포함된 보험 상품을 검토해보세요";
            case GAP_ORIENTAL      -> "한방 특약이 포함된 실손보험을 검토해보세요";
            case GAP_OPHTHALMOLOGY -> "시력교정·안과 관련 특약 보험을 검토해보세요";
            case GAP_DERMATOLOGY   -> "피부 관련 특약이 포함된 보험 상품을 검토해보세요";
            default                -> "해당 진료 분야 보험 상품을 검토해보세요";
        };
    }
}
