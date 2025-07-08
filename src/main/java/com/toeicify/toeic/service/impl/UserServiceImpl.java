package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.dto.request.user.UpdateUserRequest;
import com.toeicify.toeic.dto.response.user.UserUpdateResponse;
import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.exception.ResourceAlreadyExistsException;
import com.toeicify.toeic.exception.ResourceNotFoundException;
import com.toeicify.toeic.mapper.UserMapper;
import com.toeicify.toeic.repository.RoleRepository;
import com.toeicify.toeic.repository.UserRepository;
import com.toeicify.toeic.service.UserService;
import com.toeicify.toeic.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    public User findByUsernameOrEmail(String identifier) {
        return userRepository.findByUsernameOrEmail(identifier, identifier).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public User processOAuth2User(String email, String name, String socialId, String provider) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        User newUser = User.builder()
                .email(email)
                .username(email)
                .fullName(name)
                .role(roleRepository.findById("GUEST").orElseThrow(() -> new ResourceNotFoundException("Default role not found")))
                .registrationDate(Instant.now())
                .socialMediaId(socialId)
                .socialMediaProvider(provider)
                .build();
        return userRepository.save(newUser);
    }

    @Override
    public User findById(Long uid) {
        return userRepository.findById(uid).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public UserUpdateResponse updateCurrentUser(UpdateUserRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.getUsername().equals(request.username()) &&
                userRepository.existsByUsername(request.username())) {
            throw new ResourceAlreadyExistsException("Username is already in use");
        }
        if (!user.getEmail().equals(request.email()) &&
                userRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("Email is already in use");
        }
        user.setFullName(request.fullName());
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setTargetScore(request.targetScore());
        user.setExamDate(request.examDate());
        userRepository.save(user);
        return userMapper.toUserUpdateResponse(user);
    }
}
