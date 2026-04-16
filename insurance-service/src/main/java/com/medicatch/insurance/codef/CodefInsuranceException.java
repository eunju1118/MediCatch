package com.medicatch.insurance.codef;

import lombok.Getter;

@Getter
public class CodefInsuranceException extends RuntimeException {

    private final String resultCode;
    private final String productUrl;

    public CodefInsuranceException(String message) {
        super(message);
        this.resultCode = null;
        this.productUrl = null;
    }

    public CodefInsuranceException(String message, Throwable cause) {
        super(message, cause);
        this.resultCode = null;
        this.productUrl = null;
    }

    public CodefInsuranceException(String message, String resultCode, String productUrl) {
        super(message);
        this.resultCode = resultCode;
        this.productUrl = productUrl;
    }

    public CodefInsuranceException(String message, String resultCode, String productUrl, Throwable cause) {
        super(message, cause);
        this.resultCode = resultCode;
        this.productUrl = productUrl;
    }

    public static CodefInsuranceException fromResponse(CodefResponse response, String productUrl) {
        String message = String.format("CODEF API 오류 [%s]: %s",
                response.getResultCode(), response.getResultMessage());
        return new CodefInsuranceException(message, response.getResultCode(), productUrl);
    }
}
