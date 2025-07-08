package com.toeicify.toeic.service;

import com.toeicify.toeic.dto.request.user.UpdateUserRequest;
import com.toeicify.toeic.dto.response.user.UserUpdateResponse;
import com.toeicify.toeic.entity.User;


public interface UserService {
    User findByUsernameOrEmail(String identifier);

    User processOAuth2User(String email, String name, String socialId, String provider);

    User findById(Long uid);
    UserUpdateResponse updateCurrentUser(UpdateUserRequest request);
}
