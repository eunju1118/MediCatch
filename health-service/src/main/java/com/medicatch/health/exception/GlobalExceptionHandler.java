package com.medicatch.health.exception;

import com.medicatch.health.codef.CodefApiException;
import com.medicatch.health.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리기
 *
 * <p>컨트롤러에서 발생하는 예외를 일관된 {@link ApiResponse} 형식으로 변환한다.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * CODEF API 호출 오류
     * HTTP 502 Bad Gateway — 외부 API 오류는 upstream 문제로 분류
     */
    @ExceptionHandler(CodefApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleCodefApiException(CodefApiException e) {
        log.error("CODEF API 오류: code={}, url={}, message={}",
                e.getResultCode(), e.getProductUrl(), e.getMessage());

        String message = buildCodefErrorMessage(e);
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error(message));
    }

    /**
     * 요청 유효성 검사 오류 (@Valid 실패)
     * HTTP 400 Bad Request
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
     * 회원가입/로그인 비즈니스 오류 (중복 ID, 비밀번호 불일치 등)
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("요청 처리 오류: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
    }

    /**
     * 그 외 예상치 못한 예외
     * HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("처리되지 않은 예외: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
    }

    // ── 내부 유틸 ─────────────────────────────────────────────────────────

    private String buildCodefErrorMessage(CodefApiException e) {
        StringBuilder sb = new StringBuilder("CODEF API 오류");
        if (e.getResultCode() != null) {
            sb.append(" [").append(e.getResultCode()).append("]");
        }
        sb.append(": ").append(e.getMessage());
        return sb.toString();
    }
}
