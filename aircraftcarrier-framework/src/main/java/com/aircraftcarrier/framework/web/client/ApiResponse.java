package com.aircraftcarrier.framework.web.client;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author mastercard
 */
@Data
public class ApiResponse<T> {
    private final int statusCode;
    private final String message;
    private final Map<String, List<String>> headers;
    private final T data;

    public ApiResponse(int statusCode, String message, Map<String, List<String>> headers, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.headers = headers;
        this.data = data;
    }
}
