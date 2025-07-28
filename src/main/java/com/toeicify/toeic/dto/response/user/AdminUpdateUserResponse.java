package com.toeicify.toeic.dto.response.user;

import com.toeicify.toeic.entity.User;

import java.time.Instant;

public record AdminUpdateUserResponse(
        Long userId,
        String username,
        String email,
        String fullName,
        Integer targetScore,
        String roleId,
        String roleName,
        Instant examDate,
        Boolean isActive,
        Instant registrationDate,
        String lockReason
) {
    public static AdminUpdateUserResponse from(User user) {
        return new AdminUpdateUserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getTargetScore(),
                user.getRole() != null ? user.getRole().getRoleId() : null,
                user.getRole() != null ? user.getRole().getRoleName() : null,
                user.getExamDate(),
                user.getIsActive(),
                user.getRegistrationDate(),
                user.getLockReason()
        );
    }
}