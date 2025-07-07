package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.exception.ResourceNotFoundException;
import com.toeicify.toeic.repository.UserRepository;
import com.toeicify.toeic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User findByUsernameOrEmail(String identifier) {
        return userRepository.findByUsernameOrEmail(identifier, identifier).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
