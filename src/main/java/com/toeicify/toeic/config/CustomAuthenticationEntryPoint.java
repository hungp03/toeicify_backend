package com.toeicify.toeic.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeicify.toeic.dto.response.ApiResponse;
import com.toeicify.toeic.util.constant.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by hungpham on 7/7/2025
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        ApiResponse<Object> apiResponse = new ApiResponse<>(
                false,
                ErrorCode.UNAUTHORIZED,
                null,
                "Token is not valid or user is not logged in",
                "Unauthorized"
        );
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
    }
}
