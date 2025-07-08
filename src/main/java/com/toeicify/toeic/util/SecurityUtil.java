package com.toeicify.toeic.util;

import com.toeicify.toeic.config.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Created by hungpham on 7/7/2025
 */
public class SecurityUtil {
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            try {
                return Long.parseLong(jwt.getSubject());
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid subject format in JWT");
            }
        }
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser().getUserId();
        }
        throw new RuntimeException("Cannot determine user ID from authentication");
    }
}

