package com.toeicify.toeic.dto.request.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/**
 * Created by hungpham on 8/19/2025
 */
public record FeedbackRequest(@NotBlank
                              @Pattern(
                                      regexp = "^[\\p{L}\\p{N}\\s.,!?\"'():;\\-…“”‘’\\n\\r\\t]+$",
                                      flags = { Pattern.Flag.UNICODE_CASE },
                                      message = "Content must contain only letters, numbers, spaces and punctuation"
                              )
                              String content,
                              List<String> attachments){
}
