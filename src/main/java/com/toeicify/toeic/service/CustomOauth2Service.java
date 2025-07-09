package com.toeicify.toeic.service;

import com.toeicify.toeic.entity.User;

/**
 * Created by hungpham on 7/9/2025
 */
public interface CustomOauth2Service {
    User processOAuth2User(String email, String name, String socialId, String provider);
}
