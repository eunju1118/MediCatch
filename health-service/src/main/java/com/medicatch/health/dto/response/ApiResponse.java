package com.medicatch.health.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /** 2-Way 인증 필요 시 응답 */
    public static <T> ApiResponse<T> twoWayRequired(T twoWayInfo) {
        return new ApiResponse<>(true, "2-Way 인증이 필요합니다", twoWayInfo);
    }
}
