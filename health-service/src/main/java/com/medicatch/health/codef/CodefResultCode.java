package com.medicatch.health.codef;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * CODEF API 결과 코드 열거형
 * API 응답의 result.code 값과 매핑
 */
@Getter
@RequiredArgsConstructor
public enum CodefResultCode {

    // ── 성공 ────────────────────────────────────────────────────────────
    SUCCESS("CF-00000", "성공"),

    // ── 2-Way 인증 ────────────────────────────────────────────────────────
    TWO_WAY_REQUIRED("CF-03002", "추가 인증 필요 — 간편인증(카카오/PASS/SMS) 대기"),

    // ── 인증/토큰 오류 ────────────────────────────────────────────────────
    INVALID_TOKEN("CF-00400", "유효하지 않은 접근 토큰"),
    AUTH_FAILED("CF-00401", "인증 실패"),
    TOKEN_EXPIRED("CF-00403", "접근 토큰 만료"),

    // ── 커넥티드 아이디 ───────────────────────────────────────────────────
    INVALID_CONNECTED_ID("CF-01002", "유효하지 않은 커넥티드 아이디"),

    // ── 서비스 상태 ───────────────────────────────────────────────────────
    TIMEOUT("CF-04000", "서비스 응답 타임아웃"),
    MAINTENANCE("CF-05000", "외부 기관 서비스 점검 중"),

    // ── 시스템 오류 ───────────────────────────────────────────────────────
    SYSTEM_ERROR("CF-99999", "시스템 오류"),

    // ── 파싱 실패 / 알 수 없음 ────────────────────────────────────────────
    PARSE_ERROR("PARSE_ERROR", "응답 JSON 파싱 실패"),
    UNKNOWN("UNKNOWN", "알 수 없는 결과 코드");

    private final String code;
    private final String description;

    /**
     * 코드 문자열로 열거 상수 조회 — 미등록 코드는 UNKNOWN 반환
     */
    public static CodefResultCode of(String code) {
        return Arrays.stream(values())
                .filter(r -> r.code.equals(code))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public boolean is(CodefResultCode other) {
        return this == other;
    }
}
