package com.toeicify.toeic.service;

/**
 * Created by hungpham on 7/9/2025
 */
public interface IdentifyCodeService {
    String generateOTP(String email);
    boolean validateCode(String email, String input);
    void deleteCode(String key);
}
