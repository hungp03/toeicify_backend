package com.toeicify.toeic.service;

import com.toeicify.toeic.entity.User;


public interface UserService {
    User findByUsernameOrEmail(String identifier);
}
