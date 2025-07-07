package com.toeicify.toeic.dto.response;

public record ApiResponse<T>(
        boolean success,
        int code,
        T data,
        Object message,
        String error
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, 200, data, null, null);
    }

    public static <T> ApiResponse<T> error(int code, Object message, String error) {
        return new ApiResponse<>(false, code, null, message, error);
    }
}
