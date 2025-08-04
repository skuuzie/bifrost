package com.bifrost.demo.controller;

import com.bifrost.demo.annotation.DailyLimit;
import com.bifrost.demo.dto.model.BifrostUser;
import com.bifrost.demo.dto.request.GetTemporaryAdminTokenRequest;
import com.bifrost.demo.dto.request.TemporaryAdminTokenRequest;
import com.bifrost.demo.dto.response.BaseResponse;
import com.bifrost.demo.dto.response.ServiceResponse;
import com.bifrost.demo.service.authentication.JWTService;
import com.bifrost.demo.service.authentication.OTPService;
import com.bifrost.demo.service.authentication.RedisOTPService;
import com.bifrost.demo.service.authentication.TokenService;
import com.bifrost.demo.service.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/v1")
public class UserController {
    private final OTPService otpService;
    private final TokenService tokenService;

    public UserController(
            RedisOTPService redisOTPService,
            JWTService jwtService) {
        this.otpService = redisOTPService;
        this.tokenService = jwtService;
    }

    @DailyLimit(id = "registerToken", max = 3)
    @PostMapping("/register-token")
    public ResponseEntity<BaseResponse> registerToken(
            @Parameter(
                    in = ParameterIn.HEADER,
                    name = "X-Role",
                    required = true,
                    description = "Selectable role header",
                    schema = @Schema(type = "string", allowableValues = {"TEMPORARY_ADMIN", "LV0_USER"})
            )
            @RequestHeader("X-Role") String role,
            @RequestBody TemporaryAdminTokenRequest req
    ) {

        Optional<BifrostUser.Role> _role = BifrostUser.Role.validateRegisterInput(role);

        if (_role.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.error("Role is not whitelisted."));
        }

        ServiceResponse<String> res = otpService.createNewOTP(
                String.format("%s::%s", role, req.username()), req.email());

        if (res.isSuccess()) {
            return ResponseEntity
                    .ok(BaseResponse.success("OTP sent successfully.", res.getData()));
        } else {
            return ResponseUtil.processError(res);
        }
    }

    @DailyLimit(id = "claimToken", max = 10)
    @PostMapping("/claim-token")
    public ResponseEntity<BaseResponse> claimToken(
            @Parameter(
                    in = ParameterIn.HEADER,
                    name = "X-Role",
                    required = true,
                    description = "Selectable role header",
                    schema = @Schema(type = "string", allowableValues = {"TEMPORARY_ADMIN", "LV0_USER"})
            )
            @RequestHeader("X-Role") String role,
            @RequestBody GetTemporaryAdminTokenRequest req
    ) {
        ServiceResponse<Boolean> validateOTP = otpService.validateOTP(
                String.format("%s::%s", role, req.username()), req.otp());

        if (!validateOTP.isSuccess()) {
            return ResponseUtil.processError(validateOTP);
        }

        Optional<BifrostUser.Role> _role = BifrostUser.Role.validateRegisterInput(role);

        if (_role.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.error("Role is not whitelisted."));
        }

        ServiceResponse<BifrostUser> user = tokenService.createToken(req.username(), _role.get());
        if (!user.isSuccess()) {
            return ResponseUtil.processError(user);
        }

        return ResponseEntity.ok(BaseResponse.success("Token created.", user.getData()));
    }
}
