package com.medicatch.health.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicatch.health.codef.*;
import com.medicatch.health.dto.request.MedicalInfoCertifyRequest;
import com.medicatch.health.dto.request.MedicalInfoRequest;
import com.medicatch.health.dto.response.medical.MedicalInfoResponse;
import com.medicatch.health.dto.response.medical.MedicalInfoResult;
import com.medicatch.health.store.HealthDataStore;
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
    private final HealthDataStore healthDataStore;

    // ── 1차 요청 ──────────────────────────────────────────────────────────

    public MedicalInfoResult requestInfo(MedicalInfoRequest request, String userId) {
        log.info("진료정보 열람 1차 요청: user={}, period={}-{}",
                request.getUserName(), request.getStartDate(), request.getEndDate());

        CodefResponse response = twoWayAuthHandler.executeFirst(
                PRODUCT_URL, buildParams(request));

        if (response.isTwoWayRequired()) {
            TwoWayContext ctx = twoWayAuthHandler.extractContext(response);
            log.info("진료정보 열람 — 간편인증 대기: jti={}", ctx.getJti());
            return MedicalInfoResult.pending(ctx, response.getResultCode(), response.getResultMessage());
        }

        if (response.isError()) {
            log.error("진료정보 열람 1차 요청 오류: code={}, msg={}",
                    response.getResultCode(), response.getResultMessage());
            throw CodefApiException.fromResponse(response, PRODUCT_URL);
        }

        MedicalInfoResponse medicalInfo = toMedicalInfoResponse(response);
        healthDataStore.storeMedicalData(userId, medicalInfo);
        return MedicalInfoResult.success(medicalInfo);
    }

    // ── 2차 인증 ──────────────────────────────────────────────────────────

    public MedicalInfoResult certifyAndFetch(MedicalInfoCertifyRequest certifyRequest, String userId) {
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

        MedicalInfoResponse medicalInfo = toMedicalInfoResponse(response);
        healthDataStore.storeMedicalData(userId, medicalInfo);
        return MedicalInfoResult.success(medicalInfo);
    }

    // ── 내부 유틸 ─────────────────────────────────────────────────────────

    private HashMap<String, Object> buildParams(MedicalInfoRequest req) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("organization",   "0020");
        params.put("loginType",      req.getLoginType());
        params.put("loginTypeLevel", req.getLoginTypeLevel());
        params.put("userName",       req.getUserName());
        params.put("identity",       req.getIdentity());
        params.put("phoneNo",        req.getPhoneNo());
        params.put("telecom",        req.getTelecom());
        params.put("authMethod",     req.getAuthMethod());
        params.put("startDate",      req.getStartDate());
        params.put("endDate",        req.getEndDate());
        params.put("type",           req.getType());
        return params;
    }

//    @SuppressWarnings("unchecked")
//    private MedicalInfoResponse toMedicalInfoResponse(CodefResponse response) {
//        try {
//            List<Map<String, Object>> dataList = response.dataAsList();
//            if (dataList == null || dataList.isEmpty()) {
//                log.warn("진료정보 응답 data 배열이 비어있음");
//                return new MedicalInfoResponse();
//            }
//            MedicalInfoResponse result = objectMapper.convertValue(
//                    dataList.get(0), MedicalInfoResponse.class);
//            log.info("진료정보 파싱 완료: user={}, basicTreat={}건, prescribeDrug={}건",
//                    result.getCommName(),
//                    result.getResBasicTreatList()    != null ? result.getResBasicTreatList().size()    : 0,
//                    result.getResPrescribeDrugList() != null ? result.getResPrescribeDrugList().size() : 0);
//            return result;
//        } catch (Exception e) {
//            log.error("진료정보 응답 파싱 실패: {}", e.getMessage());
//            throw new CodefApiException("진료정보 응답 변환 실패: " + e.getMessage(), e);
//        }
//    }

    // 수정 확인
    @SuppressWarnings("unchecked")
    private MedicalInfoResponse toMedicalInfoResponse(CodefResponse response) {
        try {
            // 1. response.getRawData()를 통해 data 필드를 Map(객체)으로 가져옵니다.
            Object rawData = response.getRawData();

            if (rawData == null) {
                log.warn("진료정보 응답 data가 null임");
                return new MedicalInfoResponse();
            }

            // 2. data가 Map인 경우 (정상적인 CODEF 응답 구조)
            if (rawData instanceof Map<?, ?> dataMap) {
                // ObjectMapper를 사용하여 Map을 DTO(MedicalInfoResponse)로 바로 매핑합니다.
                // 이렇게 하면 내부에 있는 resBasicTreatList 등이 자동으로 필드에 들어갑니다.
                MedicalInfoResponse result = objectMapper.convertValue(dataMap, MedicalInfoResponse.class);

                log.info("진료정보 파싱 완료: basicTreat={}건, prescribeDrug={}건",
                        result.getResBasicTreatList()    != null ? result.getResBasicTreatList().size()    : 0,
                        result.getResPrescribeDrugList() != null ? result.getResPrescribeDrugList().size() : 0);

                return result;
            }

            // 3. 만약 혹시라도 data가 List로 들어오는 경우 (하위 호환성 대비)
            if (rawData instanceof List<?> dataList && !dataList.isEmpty()) {
                return objectMapper.convertValue(dataList.get(0), MedicalInfoResponse.class);
            }

            throw new IllegalArgumentException("data 필드가 List가 아닙니다. 실제 타입: " + rawData.getClass().getSimpleName());

        } catch (Exception e) {
            log.error("진료정보 응답 파싱 실패: {}", e.getMessage());
            throw new CodefApiException("진료정보 응답 변환 실패: " + e.getMessage(), e);
        }
    }
}
