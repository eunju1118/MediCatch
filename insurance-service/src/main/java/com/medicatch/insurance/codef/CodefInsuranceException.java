package com.medicatch.insurance.codef;

public class CodefInsuranceException extends RuntimeException {

    public CodefInsuranceException(String message) {
        super(message);
    }

    public CodefInsuranceException(String message, Throwable cause) {
        super(message, cause);
    }
}
