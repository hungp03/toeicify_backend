package com.toeicify.toeic.util;

import com.toeicify.toeic.config.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Created by hungpham on 7/7/2025
 */
public class SecurityUtil {
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUsername();
        }

        if (principal instanceof String username) {
            return username;
        }

        String name = authentication.getName();
        if (name != null) {
            return name;
        }

        throw new RuntimeException("Cannot determine username from authentication");
    }
}

