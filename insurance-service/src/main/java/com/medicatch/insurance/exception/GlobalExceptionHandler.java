package com.medicatch.insurance.exception;

import com.medicatch.insurance.codef.CodefInsuranceException;
import com.medicatch.insurance.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * CODEF API 호출 오류 — HTTP 502 Bad Gateway
     */
    @ExceptionHandler(CodefInsuranceException.class)
    public ResponseEntity<ApiResponse<Void>> handleCodefInsuranceException(CodefInsuranceException e) {
        log.error("CODEF API 오류: code={}, url={}, message={}",
                e.getResultCode(), e.getProductUrl(), e.getMessage());
        String message = buildCodefErrorMessage(e);
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error(message));
    }

    /**
     * 요청 유효성 검사 오류 (@Valid) — HTTP 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e) {

        String details = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> "[" + fe.getField() + "] " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("요청 유효성 검사 실패: {}", details);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("입력값 오류: " + details));
    }

    /**
     * 예상치 못한 예외 — HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("처리되지 않은 예외: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
    }

    private String buildCodefErrorMessage(CodefInsuranceException e) {
        StringBuilder sb = new StringBuilder("CODEF API 오류");
        if (e.getResultCode() != null) {
            sb.append(" [").append(e.getResultCode()).append("]");
        }
        sb.append(": ").append(e.getMessage());
        return sb.toString();
    }
}
