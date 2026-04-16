package com.medicatch.health.dto.response.medical;

import com.medicatch.health.codef.TwoWayContext;
import lombok.Builder;
import lombok.Getter;

/**
 * 내 진료정보 열람 서비스 반환 타입
 *
 * <p>1차 요청과 2차 인증 모두 이 타입을 반환하며,
 * {@code twoWayRequired} 플래그로 프론트엔드가 다음 동작을 결정한다.</p>
 *
 * <pre>
 * twoWayRequired = true  → twoWayContext의 값을 저장 후 사용자 간편인증 화면 표시
 *                           인증 완료 후 /info/certify 엔드포인트 호출
 *
 * twoWayRequired = false → medicalInfo에 최종 진료 데이터 포함
 * </pre>
 *
 * <p>컨트롤러 응답 예시 (2-Way 대기):</p>
 * <pre>
 * {
 *   "success": true,
 *   "message": "OK",
 *   "data": {
 *     "twoWayRequired": true,
 *     "twoWayContext": { "jobIndex": 0, "threadIndex": 0, "jti": "...", "twoWayTimestamp": 000 },
 *     "medicalInfo":   null,
 *     "resultCode":    "CF-03002",
 *     "resultMessage": "추가 인증이 필요합니다"
 *   }
 * }
 * </pre>
 *
 * <p>컨트롤러 응답 예시 (성공):</p>
 * <pre>
 * {
 *   "success": true,
 *   "message": "OK",
 *   "data": {
 *     "twoWayRequired": false,
 *     "twoWayContext":  null,
 *     "medicalInfo": {
 *       "commName":          "홍길동",
 *       "commStartDate":     "20240101",
 *       "commEndDate":       "20241231",
 *       "resBasicTreatList": [ ... ],
 *       ...
 *     },
 *     "resultCode":    "CF-00000",
 *     "resultMessage": "성공"
 *   }
 * }
 * </pre>
 */
@Getter
@Builder
public class MedicalInfoResult {

    /**
     * 간편인증 대기 여부
     * - true  : twoWayContext 참조 후 사용자 인증 진행
     * - false : medicalInfo에 데이터 포함
     */
    private final boolean twoWayRequired;

    /**
     * 2-Way 인증 메타데이터 (twoWayRequired=true 일 때만 non-null)
     * 프론트엔드에서 /info/certify 호출 시 그대로 전달
     */
    private final TwoWayContext twoWayContext;

    /**
     * 최종 진료정보 데이터 (twoWayRequired=false 일 때만 non-null)
     */
    private final MedicalInfoResponse medicalInfo;

    /** CODEF 결과 코드 */
    private final String resultCode;

    /** CODEF 결과 메시지 */
    private final String resultMessage;

    // ── 팩토리 메서드 ─────────────────────────────────────────────────────

    /** 간편인증 대기 결과 생성 */
    public static MedicalInfoResult pending(TwoWayContext ctx,
                                            String resultCode,
                                            String resultMessage) {
        return MedicalInfoResult.builder()
                .twoWayRequired(true)
                .twoWayContext(ctx)
                .resultCode(resultCode)
                .resultMessage(resultMessage)
                .build();
    }

    /** 조회 성공 결과 생성 */
    public static MedicalInfoResult success(MedicalInfoResponse medicalInfo) {
        return MedicalInfoResult.builder()
                .twoWayRequired(false)
                .medicalInfo(medicalInfo)
                .resultCode("CF-00000")
                .resultMessage("성공")
                .build();
    }
}
