package com.medicatch.health.codef;

import lombok.Getter;

/**
 * CODEF API 호출 관련 예외
 *
 * <p>resultCode와 productUrl을 함께 담아 로그 및 에러 응답 구성에 활용한다.</p>
 */
@Getter
public class CodefApiException extends RuntimeException {

    /** CODEF 결과 코드 (예: CF-03002, PARSE_ERROR) — 알 수 없으면 null */
    private final String resultCode;

    /** 호출한 CODEF 상품 URL — 해당 없으면 null */
    private final String productUrl;

    public CodefApiException(String message) {
        super(message);
        this.resultCode = null;
        this.productUrl = null;
    }

    public CodefApiException(String message, Throwable cause) {
        super(message, cause);
        this.resultCode = null;
        this.productUrl = null;
    }

    public CodefApiException(String message, String resultCode, String productUrl) {
        super(message);
        this.resultCode = resultCode;
        this.productUrl = productUrl;
    }

    public CodefApiException(String message, String resultCode, String productUrl, Throwable cause) {
        super(message, cause);
        this.resultCode = resultCode;
        this.productUrl = productUrl;
    }

    /**
     * {@link CodefResponse}에서 직접 예외 생성
     * isError() == true 인 응답을 예외로 변환할 때 사용
     */
    public static CodefApiException fromResponse(CodefResponse response, String productUrl) {
        return new CodefApiException(
                String.format("[%s] %s (url=%s)",
                        response.getResultCode(), response.getResultMessage(), productUrl),
                response.getResultCode(),
                productUrl
        );
    }
}
