package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
/**
 * Created by hungpham on 7/8/2025
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean isBlacklisted(String jti) {
        return redisTemplate.hasKey("rt_revoked:" + jti) || redisTemplate.hasKey("at_revoked:" + jti);
    }
}
