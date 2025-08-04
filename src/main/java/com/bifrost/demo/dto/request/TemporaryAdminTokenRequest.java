package com.bifrost.demo.dto.request;

public record TemporaryAdminTokenRequest(
        String username,
        String email
) {
}