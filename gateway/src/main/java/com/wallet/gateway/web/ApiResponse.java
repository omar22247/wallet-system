package com.wallet.gateway.web;

import java.time.Instant;
import java.util.List;


public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        List<String> errors,
        Instant timestamp,
        String path
) {
    public static <T> ApiResponse<T> error(String message, String path) {
        return new ApiResponse<>(false, message, null, List.of(), Instant.now(), path);
    }
}
