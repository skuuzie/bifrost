package com.bifrost.demo.dto.model;

public record OTP(
        String code,
        int tryCount,
        long creationTime
) {
}
