package com.toeicify.toeic.dto.response.user;

import lombok.*;

@Getter
@Setter
@Builder
public class UserLoginResponse {
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private RoleResponse role;
    @Getter
    @Setter
    @Builder
    public static class RoleResponse {
        private String roleId;
        private String roleName;
    }
}
