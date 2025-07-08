package com.toeicify.toeic.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeicify.toeic.dto.response.ApiResponse;
import com.toeicify.toeic.util.constant.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        String message = "Token is not valid";
        String error = "Unauthorized";

        if (authException instanceof OAuth2AuthenticationException oAuthEx) {
            OAuth2Error oAuthError = oAuthEx.getError();
            if (oAuthError != null) {
                error = oAuthError.getErrorCode();
                message = oAuthError.getDescription();
            }
        }

        ApiResponse<Object> apiResponse = new ApiResponse<>(
                false,
                ErrorCode.UNAUTHORIZED,
                null,
                message,
                error
        );

        mapper.writeValue(response.getWriter(), apiResponse);
    }
}
