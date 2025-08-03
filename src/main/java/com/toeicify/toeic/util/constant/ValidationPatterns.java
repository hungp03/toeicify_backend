package com.toeicify.toeic.util.constant;

/**
 * Created by hungpham on 7/9/2025
 */
public interface ValidationPatterns {
    String FULL_NAME_PATTERN = "^[a-zA-Z\\p{L}\\s]+$";
    String USERNAME_PATTERN = "^[a-zA-Z0-9._@+-]+$";
    String EMAIL_PATTERN = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$";
    String PASSWORD_PATTERN = "^[^\\s]+$";
    String IDENTIFIER_PATTERN = "^[a-zA-Z0-9._+\\-@]+$";
    String OTP_PATTERN = "^[A-Z0-9]{6}$";
    String CATEGORY_PATTERN = "^[\\p{L}\\p{N} .!_]+$";
}
