package com.medicatch.health.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 건강 통합 리포트 응답 DTO.
 *
 * <p>진료기록(HealthDataStore)과 보험 계약정보(InsuranceServiceClient)를
 * 교차 분석하여 생성된다.</p>
 */
@Getter
@Builder
public class HealthReportResponse {

    /** 분석 대상 유저 ID */
    private final String userId;

    /** 분석 기간 (개월) */
    private final int analyzedMonths;

    /** 리포트 생성 시각 */
    private final LocalDateTime generatedAt;

    // ── 진료 통계 ──────────────────────────────────────────────────────────

    /**
     * 월별 병원 방문 횟수.
     * key: "yyyy-MM", value: 방문 건수
     */
    private final Map<String, Integer> monthlyVisitCount;

    /**
     * 진료과별 이용 현황.
     * key: 진료과명, value: 방문 건수 (내림차순 정렬)
     */
    private final Map<String, Integer> departmentUsage;

    /** 총 진료비 합계 (원) */
    private final long totalMedicalCost;

    /** 총 본인부담금 합계 (원) */
    private final long totalPatientCost;

    /** 분석 기간 내 총 방문 건수 */
    private final int totalVisitCount;

    // ── 보험 분석 ──────────────────────────────────────────────────────────

    /**
     * 보험 청구 가능 항목 리스트.
     * 본인부담금 내림차순 정렬.
     */
    private final List<InsuranceClaimableItem> insuranceClaimable;

    /**
     * 보장 공백 항목 리스트.
     * 진료 기록에는 있으나 보험 미가입 영역.
     * 본인부담금 내림차순 정렬.
     */
    private final List<CoverageGapItem> coverageGap;

    /** 보험 데이터 조회 성공 여부 */
    private final boolean insuranceDataAvailable;
}
