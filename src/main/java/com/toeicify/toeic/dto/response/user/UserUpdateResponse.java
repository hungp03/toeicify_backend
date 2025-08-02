package com.toeicify.toeic.dto.response.user;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Created by hungpham on 7/9/2025
 */
public record UserUpdateResponse(String fullName,
                                 String username,
                                 String email,
                                 LocalDate examDate,
                                 Integer targetScore) {
}
