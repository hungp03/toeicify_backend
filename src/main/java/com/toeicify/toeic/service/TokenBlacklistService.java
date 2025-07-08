package com.toeicify.toeic.service;

/**
 * Created by hungpham on 7/8/2025
 */
public interface TokenBlacklistService {
    boolean isBlacklisted(String jti);
}
