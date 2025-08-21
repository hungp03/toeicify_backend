package com.toeicify.toeic.dto.response.gemini;

/**
 * Created by hungpham on 8/21/2025
 */
import java.util.List;

public record GeminiResponse(List<Candidate> candidates) {

    public record Candidate(Content content, String finishReason) {}

    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    // lấy text đầu tiên (nếu có)
    public String firstText() {
        if (candidates == null || candidates.isEmpty()) return null;
        var parts = candidates.getFirst().content().parts();
        return (parts != null && !parts.isEmpty()) ? parts.getFirst().text() : null;
    }
}

