package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.exception.ResourceNotFoundException;
import com.toeicify.toeic.repository.RoleRepository;
import com.toeicify.toeic.repository.UserRepository;
import com.toeicify.toeic.service.CustomOauth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Created by hungpham on 7/9/2025
 */
@Service
@RequiredArgsConstructor
public class CustomOauth2ServiceImpl implements CustomOauth2Service {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
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

}
