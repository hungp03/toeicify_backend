package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.RefreshToken;
import com.toeicify.toeic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by hungpham on 7/7/2025
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findAllByUser(User user);
    void deleteAllByUser(User user);
}

