package com.medicatch.health.service;

import com.medicatch.health.codef.CodefClient;
import com.medicatch.health.codef.CodefResponse;
import com.medicatch.health.codef.TwoWayAuthHandler;
import com.medicatch.health.codef.TwoWayContext;
import com.medicatch.health.dto.request.MedicalRequest;
import com.medicatch.health.dto.request.TwoWayRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 진료기록 조회 서비스
 * CODEF API: /v1/kr/public/hw/hira-list/my-medical-information (organization: 0020)
 *
 * <p>출력 필드:</p>
 * <ul>
 *   <li>resBasicTreatList  — 기본진료내역 (병원명, 진료과, 질병코드, 본인부담금)</li>
 *   <li>resDetailTreatList — 세부진료정보 (영상진단, 수술 등)</li>
 *   <li>resPrescribeDrugList — 처방조제정보 (약품명, 성분, 복용일수)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalService {

    private static final String PRODUCT_URL =
            "/v1/kr/public/hw/hira-list/my-medical-information";

    private final CodefClient codefClient;
    private final TwoWayAuthHandler twoWayAuthHandler;

    // ── 1차 요청 ──────────────────────────────────────────────────────────

    /**
     * 진료기록 조회 1차 요청
     *
     * <p>응답 맵의 {@code isTwoWayRequired} 값이 {@code true} 이면
     * 프론트엔드가 사용자 간편인증을 수행한 뒤 {@link #certifyAndFetch}를 호출해야 한다.</p>
     *
     * @return 결과 맵
     *   <ul>
     *     <li>{@code resultCode}       — CODEF 결과 코드</li>
     *     <li>{@code resultMessage}    — CODEF 결과 메시지</li>
     *     <li>{@code isTwoWayRequired} — 간편인증 대기 여부</li>
     *     <li>{@code data}             — 성공 시 진료기록 데이터 / CF-03002 시 2-Way 메타데이터</li>
     *   </ul>
     */
    public Map<String, Object> requestMedicalInfo(MedicalRequest request) {
        log.info("진료기록 조회 1차 요청: user={}", request.getUserName());
        CodefResponse response = twoWayAuthHandler.executeFirst(PRODUCT_URL, buildParams(request));
        return toResponseMap(response);
    }

    // ── 2차 인증 ──────────────────────────────────────────────────────────

    /**
     * 진료기록 조회 2차 인증 — 간편인증 완료 후 최종 데이터 수신
     *
     * @param twoWayRequest 1차 응답에서 전달받은 2-Way 메타데이터 (jobIndex, threadIndex, jti, twoWayTimestamp)
     * @param original      1차 요청과 동일한 원본 요청 객체
     */
    public Map<String, Object> certifyAndFetch(TwoWayRequest twoWayRequest, MedicalRequest original) {
        log.info("진료기록 조회 2차 인증: jti={}", twoWayRequest.getJti());
        TwoWayContext ctx = TwoWayContext.from(twoWayRequest);
        CodefResponse response = twoWayAuthHandler.executeSecond(PRODUCT_URL, buildParams(original), ctx);
        return toResponseMap(response);
    }

    // ── 내부 유틸 ─────────────────────────────────────────────────────────

    private HashMap<String, Object> buildParams(MedicalRequest req) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("organization",    "0020");
        params.put("loginType",       req.getLoginType());
        params.put("loginTypeLevel",  req.getLoginTypeLevel());
        params.put("userName",        req.getUserName());
        params.put("identity",        req.getIdentity());
        params.put("phoneNo",         req.getPhoneNo());
        params.put("telecom",         req.getTelecom());
        params.put("authMethod",      req.getAuthMethod());
        params.put("startDate",       req.getStartDate());
        params.put("endDate",         req.getEndDate());
        params.put("type",            req.getType());
        return params;
    }

    /**
     * CodefResponse → 컨트롤러 반환용 맵 변환
     * isTwoWayRequired 필드를 포함시켜 프론트가 분기 처리할 수 있도록 한다.
     */
    private Map<String, Object> toResponseMap(CodefResponse response) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("resultCode",       response.getResultCode());
        map.put("resultMessage",    response.getResultMessage());
        map.put("isTwoWayRequired", response.isTwoWayRequired());
        map.put("data",             response.getRawData());

        if (response.isError()) {
            log.warn("진료기록 조회 오류 응답: code={}, message={}",
                    response.getResultCode(), response.getResultMessage());
        }
        return map;
    }
}
