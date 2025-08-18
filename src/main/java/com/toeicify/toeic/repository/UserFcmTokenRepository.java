package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by hungpham on 8/18/2025
 */
@Repository
public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {
    List<UserFcmToken> findByUserId(Long userId);
    boolean existsByUserIdAndToken(Long userId, String token);
}
