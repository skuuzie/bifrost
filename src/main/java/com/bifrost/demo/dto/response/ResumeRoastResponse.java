package com.bifrost.demo.dto.response;

public record ResumeRoastResponse(
        String id,
        String status,
        String result,
        Object lastUpdated
) {
}
