package com.bifrost.demo.dto.response;

public record BaseResponse(
        String status,
        String message,
        Object data
) {
    public static BaseResponse success(String message, Object data) {
        return new BaseResponse("SUCCESS", message, data);
    }

    public static BaseResponse error(String message) {
        return new BaseResponse("ERROR", message, null);
    }
}
