package com.toeicify.toeic.service;

import com.toeicify.toeic.domain.User;

import java.util.Optional;

public interface UserService {
    User findByUsername(String username);
}
