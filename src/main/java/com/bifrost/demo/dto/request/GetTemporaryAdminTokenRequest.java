package com.bifrost.demo.dto.request;

public record GetTemporaryAdminTokenRequest(
        String username,
        String otp
) {
}
