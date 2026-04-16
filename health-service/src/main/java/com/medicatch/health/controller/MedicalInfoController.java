package com.medicatch.health.controller;

import com.medicatch.health.dto.request.MedicalInfoCertifyRequest;
import com.medicatch.health.dto.request.MedicalInfoRequest;
import com.medicatch.health.dto.response.ApiResponse;
import com.medicatch.health.dto.response.medical.MedicalInfoResult;
import com.medicatch.health.service.MedicalInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 내 진료정보 열람 컨트롤러
 *
 * <p>API Gateway 경유 실제 경로: {@code /api/health/medical/info[/certify]}</p>
 *
 * <h3>2-Way 인증 시퀀스</h3>
 * <pre>
 * [프론트] POST /api/health/medical/info
 *   ↓ { userName, identity, phoneNo, startDate, endDate, ... }
 * [서버]  1차 요청 → CODEF
 *   ↓ CF-03002 응답
 * [서버]  응답 반환 → { twoWayRequired: true, twoWayContext: { jti, ... } }
 *   ↓
 * [사용자] 카카오 / PASS / SMS 인증 수행
 *   ↓
 * [프론트] POST /api/health/medical/info/certify
 *   ↓ { original: { ...1차 요청과 동일... }, jti: "...", jobIndex: 0, ... }
 * [서버]  2차 요청 → CODEF
 *   ↓ CF-00000 응답
 * [서버]  응답 반환 → { twoWayRequired: false, medicalInfo: { ... } }
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/medical")
@RequiredArgsConstructor
public class MedicalInfoController {

    private final MedicalInfoService medicalInfoService;

    /**
     * 내 진료정보 열람 1차 요청
     *
     * <p><b>Gateway 경로:</b> {@code POST /api/health/medical/info}</p>
     *
     * <p><b>요청 예시:</b></p>
     * <pre>
     * {
     *   "loginType":      "2",
     *   "loginTypeLevel": "1",
     *   "userName":       "홍길동",
     *   "identity":       "9001011234567",
     *   "phoneNo":        "01012345678",
     *   "telecom":        "0",
     *   "authMethod":     "0",
     *   "startDate":      "20240101",
     *   "endDate":        "20241231",
     *   "type":           "0"
     * }
     * </pre>
     *
     * <p><b>2-Way 대기 응답 예시:</b></p>
     * <pre>
     * {
     *   "success": true, "message": "OK",
     *   "data": {
     *     "twoWayRequired": true,
     *     "twoWayContext": { "jobIndex": 0, "threadIndex": 0,
     *                        "jti": "xxx", "twoWayTimestamp": 1234567890 },
     *     "medicalInfo": null,
     *     "resultCode": "CF-03002", "resultMessage": "추가 인증이 필요합니다"
     *   }
     * }
     * </pre>
     *
     * <p><b>성공 응답 예시:</b></p>
     * <pre>
     * {
     *   "success": true, "message": "OK",
     *   "data": {
     *     "twoWayRequired": false,
     *     "twoWayContext": null,
     *     "medicalInfo": {
     *       "commName": "홍길동",
     *       "commStartDate": "20240101", "commEndDate": "20241231",
     *       "resBasicTreatList":    [ { "reqDate": "20240315", ... } ],
     *       "resDetailTreatList":   [ { ... } ],
     *       "resPrescribeDrugList": [ { ... } ]
     *     },
     *     "resultCode": "CF-00000", "resultMessage": "성공"
     *   }
     * }
     * </pre>
     */
    @PostMapping("/info")
    public ResponseEntity<ApiResponse<MedicalInfoResult>> requestInfo(
            @Valid @RequestBody MedicalInfoRequest request) {

        log.debug("진료정보 열람 1차 요청 수신: user={}", request.getUserName());
        MedicalInfoResult result = medicalInfoService.requestInfo(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 내 진료정보 열람 2차 인증 — 간편인증 완료 후 호출
     *
     * <p><b>Gateway 경로:</b> {@code POST /api/health/medical/info/certify}</p>
     *
     * <p><b>요청 예시:</b></p>
     * <pre>
     * {
     *   "original": {
     *     "loginType": "2", "loginTypeLevel": "1",
     *     "userName":  "홍길동", "identity": "9001011234567",
     *     "phoneNo":   "01012345678", "telecom": "0", "authMethod": "0",
     *     "startDate": "20240101", "endDate": "20241231", "type": "0"
     *   },
     *   "jobIndex":        0,
     *   "threadIndex":     0,
     *   "jti":             "트랜잭션ID",
     *   "twoWayTimestamp": 1234567890000
     * }
     * </pre>
     */
    @PostMapping("/info/certify")
    public ResponseEntity<ApiResponse<MedicalInfoResult>> certify(
            @Valid @RequestBody MedicalInfoCertifyRequest request) {

        log.debug("진료정보 열람 2차 인증 수신: jti={}", request.getJti());
        MedicalInfoResult result = medicalInfoService.certifyAndFetch(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
