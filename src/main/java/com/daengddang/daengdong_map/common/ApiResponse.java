package com.daengddang.daengdong_map.common;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final String message;
    private final T data;
    private final String errorCode;

    private ApiResponse(String message, T data, String errorCode) {
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }


    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data, null);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(message, null, null);
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return new ApiResponse<>(
                errorCode.getMessage(),
                null,
                errorCode.name()
        );
    }

}