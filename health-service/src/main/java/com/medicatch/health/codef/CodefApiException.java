package com.medicatch.health.codef;

public class CodefApiException extends RuntimeException {

    public CodefApiException(String message) {
        super(message);
    }

    public CodefApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
