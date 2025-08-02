package com.toeicify.toeic.dto.response.user;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Created by hungpham on 7/7/2025
 */

public record UserInfoResponse(
        Long userId,
        String username,
        String email,
        String fullName,
        Integer targetScore,
        String roleId,
        String roleName,
        LocalDate examDate,
        Instant registrationDate
) {
}

