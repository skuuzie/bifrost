package com.bifrost.demo.controller;

import com.bifrost.demo.annotation.RequireRole;
import com.bifrost.demo.aspect.DailyLimitAspect;
import com.bifrost.demo.dto.model.BifrostUser;
import com.bifrost.demo.dto.response.BaseResponse;
import com.bifrost.demo.service.authentication.RedisOTPService;
import com.bifrost.demo.service.mailing.GmailService;
import com.bifrost.demo.service.mailing.MailingService;
import com.bifrost.demo.service.parameter.ParameterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin")
public class AdminController {
    private final ParameterRegistry parameterRegistry;
    private final DailyLimitAspect dailyLimitAspect;
    private final MailingService mailingService;

    public AdminController(
            ParameterRegistry parameterRegistry,
            DailyLimitAspect dailyLimitAspect,
            GmailService gmailService,
            RedisOTPService redisOTPService) {
        this.parameterRegistry = parameterRegistry;
        this.dailyLimitAspect = dailyLimitAspect;
        this.mailingService = gmailService;
    }

    @GetMapping("/parameter-refresh")
    @RequireRole({BifrostUser.Role.ADMIN, BifrostUser.Role.TEMPORARY_ADMIN})
    public ResponseEntity<BaseResponse> refreshParameterRegistry(
            @RequestHeader(value = "X-Token", required = true) String token
    ) {
        parameterRegistry.refresh();

        return ResponseEntity
                .ok(BaseResponse.success("Parameter refresh successful.", null));
    }

    @GetMapping("/daily-limit-refresh")
    @RequireRole({BifrostUser.Role.ADMIN, BifrostUser.Role.TEMPORARY_ADMIN})
    public ResponseEntity<BaseResponse> refreshDailyLimit(
            @RequestHeader(value = "X-Token", required = true) String token
    ) {
        dailyLimitAspect.refresh();

        return ResponseEntity
                .ok(BaseResponse.success("Daily limit refresh successful.", null));
    }

    @GetMapping("/send-otp-test")
    @RequireRole({BifrostUser.Role.SUPERADMIN})
    public ResponseEntity<BaseResponse> sendOTP(
            @RequestHeader(value = "X-Token", required = true) String token
    ) {
        mailingService.sendEmail("deeonanugrah@gmail.com", "OTP", "123");

        return ResponseEntity
                .ok(BaseResponse.success("OTP sent successfully.", null));
    }
}
