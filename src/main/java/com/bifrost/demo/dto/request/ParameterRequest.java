package com.bifrost.demo.dto.request;

public record ParameterRequest(
        String id,
        String description,
        String key,
        Object value
) {
}