package com.bifrost.demo.dto.request;

public record NewParameterRequest(
        String description,
        String key,
        Object value
) {
}