package com.toeicify.toeic.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeicify.toeic.dto.response.ApiResponse;
import com.toeicify.toeic.service.UserService;
import com.toeicify.toeic.util.SecurityUtil;
import com.toeicify.toeic.util.constant.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Created by hungpham on 8/12/2025
 */
@Component
@RequiredArgsConstructor
public class AccountLockFilter extends OncePerRequestFilter {
    private final UserService userService;
    private final ObjectMapper objectMapper;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

            Long userId = Long.valueOf(authentication.getName());

            if (!userService.isUserActive(userId)) {
                SecurityContextHolder.clearContext();
                ApiResponse<Object> apiResponse =
                        ApiResponse.error(ErrorCode.FORBIDDEN, "Account is locked", "LOCKED");

                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
