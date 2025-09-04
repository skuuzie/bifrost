package com.bifrost.demo.dto.model;

public record ResumeRoastEntry(
        String identifier,
        Status status,
        String rawResult,
        String createdAt,
        String updatedAt
) {
    public enum Status {
        NEW,
        PROCESSING,
        FAILED,
        DONE
    }
}
