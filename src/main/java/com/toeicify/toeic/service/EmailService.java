package com.toeicify.toeic.service;

/**
 * Created by hungpham on 7/9/2025
 */
public interface EmailService {
    void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml);
    void sendRegisterVerificationEmail(String email, String token, String templateName);
    void sendForgotPasswordEmail(String email, String otp, String templateName);
}
