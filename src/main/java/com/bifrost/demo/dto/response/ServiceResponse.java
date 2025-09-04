package com.bifrost.demo.dto.response;

public class ServiceResponse<T> {
    private final boolean success;
    private final T data;
    private final ServiceError error;
    private final String message;

    private ServiceResponse(boolean success, T data, ServiceError error, String message) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.message = message;
    }

    public static <T> ServiceResponse<T> success(T data) {
        return new ServiceResponse<>(true, data, ServiceError.NONE, null);
    }

    public static <T> ServiceResponse<T> failure(ServiceError error, String message) {
        return new ServiceResponse<>(false, null, error, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public ServiceError getError() {
        return error;
    }

    public enum ServiceError {
        NONE,
        SERVICE_ERROR,
        SERVER_LIMIT,
        BAD_INPUT
    }
}