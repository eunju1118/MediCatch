package com.medicatch.health.dto.response.medical;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 내 진료정보 열람 응답 DTO
 *
 * <p>CODEF API 성공 시 data[0] 항목을 매핑한다.</p>
 *
 * <p>CODEF 원본 응답 구조:</p>
 * <pre>
 * {
 *   "result": { "code": "CF-00000", "message": "성공" },
 *   "data": [
 *     {
 *       "commName":             "홍길동",
 *       "commStartDate":        "20240101",
 *       "commEndDate":          "20241231",
 *       "resBasicTreatList":    [ { ... }, ... ],
 *       "resDetailTreatList":   [ { ... }, ... ],
 *       "resPrescribeDrugList": [ { ... }, ... ]
 *     }
 *   ]
 * }
 * </pre>
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicalInfoResponse {

    /** 조회 대상자 이름 */
    private String commName;

    /** 조회 시작일 (yyyyMMdd) */
    private String commStartDate;

    /** 조회 종료일 (yyyyMMdd) */
    private String commEndDate;

    /**
     * 기본진료내역
     * 병원 방문 이력, 진료과, 질병코드, 본인부담금 등
     */
    private List<BasicTreat> resBasicTreatList;

    /**
     * 세부진료정보
     * 영상진단, 수술, 처치 등 진료 항목별 상세
     */
    private List<DetailTreat> resDetailTreatList;

    /**
     * 처방조제정보
     * 약품명, 주성분, 복용일수 등
     */
    private List<PrescribeDrug> resPrescribeDrugList;
}
