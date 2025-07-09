package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.service.IdentifyCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by hungpham on 7/9/2025
 */
@Service
@RequiredArgsConstructor
public class IdentifyCodeServiceImpl implements IdentifyCodeService {
    final int OTP_EXPIRATION_TIME = 300;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public String generateOTP(String email) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        int otpLength = 6;
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            int index = random.nextInt(characters.length());
            otp.append(characters.charAt(index));
        }

        String keyPrefix = "OTP:";
        String redisKey = keyPrefix + email;
        redisTemplate.opsForValue().set(redisKey, otp.toString());
        redisTemplate.expire(redisKey, OTP_EXPIRATION_TIME, TimeUnit.SECONDS);
        return otp.toString();
    }


    @Override
    public boolean validateCode(String k, String v) {
        String value = redisTemplate.opsForValue().get(k);
        return v.equals(value);
    }

    @Override
    public void deleteCode(String key) {
        redisTemplate.delete(key);
    }


}
