package com.bifrost.demo.service.mailing;

import com.bifrost.demo.dto.response.ServiceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class GmailService implements MailingService {
    @Value("${spring.mail.username}")
    private String senderEmail;
    private final JavaMailSender sender;

    public GmailService(JavaMailSender sender) {
        this.sender = sender;
    }

    @Override
    public ServiceResponse<Boolean> sendEmail(String to, String subject, String message) {
        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setFrom(senderEmail);
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(message);

        sender.send(mail);

        return ServiceResponse.success(true);
    }

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<ServiceResponse<Boolean>> sendEmailAsync(String to, String subject, String message) {
        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setFrom(senderEmail);
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(message);

        sender.send(mail);

        return CompletableFuture.completedFuture(ServiceResponse.success(true));
    }
}
