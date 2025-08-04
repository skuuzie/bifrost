package com.bifrost.demo.service.authentication;

import com.bifrost.demo.dto.model.JSONEntry;
import com.bifrost.demo.dto.model.OTP;
import com.bifrost.demo.dto.response.ServiceResponse;
import com.bifrost.demo.registry.ParameterRegistry;
import com.bifrost.demo.service.mailing.GmailService;
import com.bifrost.demo.service.mailing.MailingService;
import com.bifrost.demo.service.monitoring.CloudWatchService;
import com.bifrost.demo.service.monitoring.LogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static com.bifrost.demo.service.util.CryptoUtil.getRandomNumber;

@Service
public class RedisOTPService implements OTPService {
    private RedisTemplate<String, OTP> OTPTemplate;
    @Value("${admin.allow-send-email}")
    private Boolean allowSendEmail;
    @Value("${parameter.user-token-rules}")
    private String userTokenRules;
    private final ParameterRegistry parameterRegistry;
    private final MailingService emailingService;
    private final LogService log;

    private final int OTP_TTL_IN_SECONDS = 300;
    private final int MAX_OTP_RETRY = 3;

    public RedisOTPService(RedisTemplate<String, OTP> template,
                           GmailService gmailService,
                           CloudWatchService cloudWatchService,
                           ParameterRegistry parameterRegistry) {
        this.OTPTemplate = template;
        this.emailingService = gmailService;
        this.log = cloudWatchService;
        this.parameterRegistry = parameterRegistry;
    }

    public ServiceResponse<Void> precheck() {
        JSONEntry param = parameterRegistry.getJson(userTokenRules);

        if (param == null) {
            return null;
        }

        if (!param.get("allowUserTokenAuthentication").asBoolean()) {
            return ServiceResponse.failure(ServiceResponse.ServiceError.SERVER_LIMIT, "Currently user token authentication is not allowed.");
        }

        return ServiceResponse.success(null);
    }

    @Override
    public ServiceResponse<String> createNewOTP(String username, String email) {
        ServiceResponse<Void> precheck = precheck();

        if (!precheck.isSuccess()) {
            return ServiceResponse.failure(precheck.getError(), precheck.getMessage());
        }

        OTP otp = new OTP(getRandomNumber(6), 0, Instant.now().getEpochSecond());

        if (OTPTemplate.opsForValue().get(username) != null) {
            return ServiceResponse
                    .failure(ServiceResponse.ServiceError.BAD_INPUT, "Maximum OTP request exceeded, please try again later.");
        }

        OTPTemplate.opsForValue().set(username, otp, OTP_TTL_IN_SECONDS, TimeUnit.SECONDS);
        log.info(
                String.format("OTP created | %s | %s:%s", email, username, otp)
        );

        if (!allowSendEmail) {
            return ServiceResponse
                    .failure(ServiceResponse.ServiceError.SERVER_LIMIT, "OTP emailing has been disabled by administrator.");
        }

        emailingService.sendEmailAsync(email, "Your OTP", String.format("Expires in 5 minute: %s", otp.code()))
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        log.info(String.format("OTP email sent | %s | %s:%s", email, username, otp));
                    } else {
                        log.error(String.format("Failed to send email OTP | %s | %s:%s", email, username, otp));
                    }
                });

        return ServiceResponse.success(username);
    }

    @Override
    public ServiceResponse<Boolean> validateOTP(String username, String otp) {
        ServiceResponse<Void> precheck = precheck();

        if (!precheck.isSuccess()) {
            return ServiceResponse.failure(precheck.getError(), precheck.getMessage());
        }

        OTP _otp = OTPTemplate.opsForValue().get(username);

        if (_otp == null) {
            return ServiceResponse
                    .failure(ServiceResponse.ServiceError.BAD_INPUT, "OTP doesn't exist or expired.");
        }

        if (_otp.tryCount() >= MAX_OTP_RETRY) {
            return ServiceResponse
                    .failure(ServiceResponse.ServiceError.BAD_INPUT, "OTP has reached max retry.");
        }

        OTP newOTP = new OTP(_otp.code(),
                _otp.tryCount() + 1,
                _otp.creationTime());

        OTPTemplate.opsForValue().set(
                username,
                newOTP,
                OTPTemplate.getExpire(username),
                TimeUnit.SECONDS
        );

        if (!_otp.code().equals(otp)) {
            return ServiceResponse
                    .failure(ServiceResponse.ServiceError.BAD_INPUT, "Invalid OTP.");
        }

        OTPTemplate.delete(username);

        return ServiceResponse.success(true);
    }
}
