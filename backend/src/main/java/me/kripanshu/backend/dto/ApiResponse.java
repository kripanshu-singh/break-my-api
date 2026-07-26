package me.kripanshu.backend.dto;

import java.util.Map;

public record ApiResponse<T>(T data, String error, Map<String, Object> meta) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null, null);
    }

    public static <T> ApiResponse<T> success(T data, Map<String, Object> meta) {
        return new ApiResponse<>(data, null, meta);
    }

    public static <T> ApiResponse<T> error(String error) {
        return new ApiResponse<>(null, error, null);
    }
}
