package com.toeicify.toeic.projection;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by hungpham on 8/10/2025
 */
public interface UserProgressProjection {
    @JsonProperty("get_user_progress")
    String getUserProgress(Long userId, Integer limit);
}
