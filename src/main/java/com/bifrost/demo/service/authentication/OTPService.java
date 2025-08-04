package com.bifrost.demo.service.authentication;

import com.bifrost.demo.dto.response.ServiceResponse;

public interface OTPService {
    ServiceResponse<String> createNewOTP(String username, String email);

    ServiceResponse<Boolean> validateOTP(String username, String otp);
}
