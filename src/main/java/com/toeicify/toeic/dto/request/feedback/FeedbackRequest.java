package com.toeicify.toeic.dto.request.feedback;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Created by hungpham on 8/19/2025
 */
public record FeedbackRequest(@NotBlank String content,
                              List<String> attachments){
}
