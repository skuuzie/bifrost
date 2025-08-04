package com.bifrost.demo.dto.model;

public record DataEntry(
        String id,
        String description,
        String key,
        Object value,
        Object createdAt,
        Object updatedAt
) {
}
