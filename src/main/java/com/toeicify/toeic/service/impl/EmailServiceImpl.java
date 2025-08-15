package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;
import java.nio.charset.StandardCharsets;

/**
 * Created by hungpham on 7/9/2025
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.verification-link}")
    private String verificationLink;

    @Override
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage,
                    isMultipart, StandardCharsets.UTF_8.name());
            message.setFrom("noreplymail@toeicify.online");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content, isHtml);
            this.javaMailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            System.out.println(e.getMessage());
        }
    }

    @Async
    @Override
    public void sendRegisterVerificationEmail(String email, String token, String templateName) {
        String newVerificationLink = verificationLink + token;
        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("verificationLink", newVerificationLink);
        String content = this.templateEngine.process(templateName, context);
        this.sendEmail(email, "Register Account at Toeicify", content, false, true);
    }

    @Async
    @Override
    public void sendForgotPasswordEmail(String email, String otp, String templateName) {
        Context context = new Context();
        context.setVariable("EMAIL", email);
        context.setVariable("OTP", otp);
        String content = this.templateEngine.process(templateName, context);
        this.sendEmail(email, "Forgot password - Toeicify", content, false, true);
    }
}
