package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.domain.User;
import com.toeicify.toeic.repository.UserRepository;
import com.toeicify.toeic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("User not found")
        );
    }
}
