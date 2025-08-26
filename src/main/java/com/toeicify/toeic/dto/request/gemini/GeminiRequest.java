package com.toeicify.toeic.dto.request.gemini;

import java.util.List;

/**
 * Gemini API request DTO
 * Created by hungpham on 8/21/2025
 */
public record GeminiRequest(
        Content systemInstruction,
        List<Content> contents
) {
    /**
     * Build từ systemPrompt + full history (user/model)
     */
    public static GeminiRequest fromHistory(String systemPrompt, List<Content> history) {
        Content system = new Content("system", List.of(new Part(systemPrompt)));
        return new GeminiRequest(system, history);
    }

    /**
     * Build 1 message (user hoặc model).
     */
    public static Content msg(String role, String text) {
        return new Content(role, List.of(new Part(text)));
    }

    public record Content(
            String role,
            List<Part> parts
    ) {}

    public record Part(
            String text
    ) {}
}
