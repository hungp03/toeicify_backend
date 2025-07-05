package com.toeicify.toeic.dto.response.auth;

import com.toeicify.toeic.dto.response.user.UserLoginResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthResponse {
    private UserLoginResponse user;
    private String accessToken;
}
