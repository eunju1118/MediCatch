package com.medicatch.health.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicatch.health.codef.CodefApiException;
import com.medicatch.health.codef.CodefClient;
import com.medicatch.health.codef.CodefResponse;
import com.medicatch.health.codef.TwoWayAuthHandler;
import com.medicatch.health.codef.TwoWayContext;
import com.medicatch.health.dto.request.MedicalInfoCertifyRequest;
import com.medicatch.health.dto.request.MedicalInfoRequest;
import com.medicatch.health.dto.response.medical.MedicalInfoResponse;
import com.medicatch.health.dto.response.medical.MedicalInfoResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 내 진료정보 열람 서비스
 *
 * CODEF API : /v1/kr/public/hw/hira-list/my-medical-information
 * organization: 0020
 *
 * <h3>전체 플로우</h3>
 * <pre>
 * [1단계] POST /api/health/medical/info
 *   └─ requestInfo(MedicalInfoRequest)
 *        ├─ CF-00000 : MedicalInfoResult.success(data)  반환
 *        └─ CF-03002 : MedicalInfoResult.pending(ctx)   반환
 *                          └─ 프론트: 간편인증 수행
 *
 * [2단계] POST /api/health/medical/info/certify
 *   └─ certifyAndFetch(MedicalInfoCertifyRequest)
 *        └─ CF-00000 : MedicalInfoResult.success(data)  반환
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalInfoService {

    private static final String PRODUCT_URL =
            "/v1/kr/public/hw/hira-list/my-medical-information";

    private final CodefClient codefClient;
    private final TwoWayAuthHandler twoWayAuthHandler;
    private final ObjectMapper objectMapper;

    // ── 1차 요청 ──────────────────────────────────────────────────────────

    /**
     * 내 진료정보 열람 1차 요청
     *
     * @param request 조회 조건 (이름, 주민번호, 기간, 민감상병 포함 여부 등)
     * @return {@link MedicalInfoResult}
     *   <ul>
     *     <li>twoWayRequired=true  → twoWayContext를 프론트에 전달 후 certifyAndFetch 호출</li>
     *     <li>twoWayRequired=false → medicalInfo에 최종 진료 데이터 포함</li>
     *   </ul>
     */
    public MedicalInfoResult requestInfo(MedicalInfoRequest request) {
        log.info("진료정보 열람 1차 요청: user={}, period={}-{}",
                request.getUserName(), request.getStartDate(), request.getEndDate());

        CodefResponse response = twoWayAuthHandler.executeFirst(
                PRODUCT_URL, buildParams(request));

        // CF-03002: 간편인증 대기
        if (response.isTwoWayRequired()) {
            TwoWayContext ctx = twoWayAuthHandler.extractContext(response);
            log.info("진료정보 열람 — 간편인증 대기: jti={}", ctx.getJti());
            return MedicalInfoResult.pending(ctx, response.getResultCode(), response.getResultMessage());
        }

        // 오류 응답 처리
        if (response.isError()) {
            log.error("진료정보 열람 1차 요청 오류: code={}, msg={}",
                    response.getResultCode(), response.getResultMessage());
            throw CodefApiException.fromResponse(response, PRODUCT_URL);
        }

        // CF-00000: 성공 (2-Way 없이 바로 완료)
        return MedicalInfoResult.success(toMedicalInfoResponse(response));
    }

    // ── 2차 인증 ──────────────────────────────────────────────────────────

    /**
     * 내 진료정보 열람 2차 인증 — 사용자 간편인증 완료 후 최종 데이터 수신
     *
     * @param certifyRequest 원본 요청 파라미터 + 2-Way 메타데이터 (jti, jobIndex 등)
     * @return {@link MedicalInfoResult} (twoWayRequired=false, medicalInfo 포함)
     */
    public MedicalInfoResult certifyAndFetch(MedicalInfoCertifyRequest certifyRequest) {
        log.info("진료정보 열람 2차 인증: jti={}", certifyRequest.getJti());

        TwoWayContext ctx = TwoWayContext.builder()
                .jobIndex(certifyRequest.getJobIndex())
                .threadIndex(certifyRequest.getThreadIndex())
                .jti(certifyRequest.getJti())
                .twoWayTimestamp(certifyRequest.getTwoWayTimestamp())
                .build();

        CodefResponse response = twoWayAuthHandler.executeSecond(
                PRODUCT_URL,
                buildParams(certifyRequest.getOriginal()),
                ctx);

        if (response.isError()) {
            log.error("진료정보 열람 2차 인증 오류: code={}, msg={}",
                    response.getResultCode(), response.getResultMessage());
            throw CodefApiException.fromResponse(response, PRODUCT_URL);
        }

        return MedicalInfoResult.success(toMedicalInfoResponse(response));
    }

    // ── 내부 유틸 ─────────────────────────────────────────────────────────

    /**
     * MedicalInfoRequest → CODEF API 파라미터 맵 변환
     * identity는 RSA 암호화 후 전달
     */
    private HashMap<String, Object> buildParams(MedicalInfoRequest req) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("organization",   "0020");
        params.put("loginType",      req.getLoginType());
        params.put("loginTypeLevel", req.getLoginTypeLevel());
        params.put("userName",       req.getUserName());
        params.put("identity",       codefClient.encryptRSA(req.getIdentity()));
        params.put("phoneNo",        req.getPhoneNo());
        params.put("telecom",        req.getTelecom());
        params.put("authMethod",     req.getAuthMethod());
        params.put("startDate",      req.getStartDate());
        params.put("endDate",        req.getEndDate());
        params.put("type",           req.getType());
        return params;
    }

    /**
     * CodefResponse → MedicalInfoResponse 변환
     *
     * CODEF 성공 응답의 data 필드는 배열이므로 data[0]을 매핑한다.
     * {@link com.fasterxml.jackson.databind.ObjectMapper#convertValue}를 사용해
     * Map → DTO 변환을 수행한다.
     */
    @SuppressWarnings("unchecked")
    private MedicalInfoResponse toMedicalInfoResponse(CodefResponse response) {
        try {
            List<Map<String, Object>> dataList = response.dataAsList();

            if (dataList == null || dataList.isEmpty()) {
                log.warn("진료정보 응답 data 배열이 비어있음");
                return new MedicalInfoResponse();
            }

            MedicalInfoResponse result = objectMapper.convertValue(
                    dataList.get(0), MedicalInfoResponse.class);

            log.info("진료정보 파싱 완료: user={}, basicTreat={}건, prescribeDrug={}건",
                    result.getCommName(),
                    result.getResBasicTreatList()    != null ? result.getResBasicTreatList().size()    : 0,
                    result.getResPrescribeDrugList() != null ? result.getResPrescribeDrugList().size() : 0);

            return result;

        } catch (Exception e) {
            log.error("진료정보 응답 파싱 실패: {}", e.getMessage());
            throw new CodefApiException("진료정보 응답 변환 실패: " + e.getMessage(), e);
        }
    }
}
