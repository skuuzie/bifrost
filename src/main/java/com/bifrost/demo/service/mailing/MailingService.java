package com.bifrost.demo.service.mailing;


import com.bifrost.demo.dto.response.ServiceResponse;

import java.util.concurrent.CompletableFuture;

public interface MailingService {
    ServiceResponse<Boolean> sendEmail(String to, String subject, String message);

    CompletableFuture<ServiceResponse<Boolean>> sendEmailAsync(String to, String subject, String message);
}
