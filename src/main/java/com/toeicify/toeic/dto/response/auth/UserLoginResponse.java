package com.toeicify.toeic.dto.response.user;

import com.toeicify.toeic.entity.User;

public record UserLoginResponse(
        Long userId,
        String username,
        String email,
        String fullName,
        RoleResponse role,
        String accessToken
) {
    public static UserLoginResponse from(User user, String accessToken) {
        return new UserLoginResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                new RoleResponse(
                        user.getRole().getRoleId(),
                        user.getRole().getRoleName()
                ),
                accessToken
        );
    }

    public record RoleResponse(
            String roleId,
            String roleName
    ) {}
}

