package com.toeicify.toeic.util;

import com.toeicify.toeic.exception.ResourceInvalidException;
import com.toeicify.toeic.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Created by hungpham on 7/7/2025
 */
public class SecurityUtil {
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new UnauthorizedException("User is not authenticated.");
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            throw new ResourceInvalidException("Invalid user ID format.");
        }
    }
}

