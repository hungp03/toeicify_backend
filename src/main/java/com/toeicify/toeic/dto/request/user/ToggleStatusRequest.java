package com.toeicify.toeic.dto.request.user;

import jakarta.validation.constraints.NotBlank;

/**
 * Created by hungpham on 7/30/2025
 */
public record ToggleStatusRequest(
        @NotBlank(message = "Lock reason is not blank")
        String lockReason
) {
}
