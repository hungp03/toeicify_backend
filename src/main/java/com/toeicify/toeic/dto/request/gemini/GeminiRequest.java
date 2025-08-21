package com.toeicify.toeic.dto.request.gemini;

/**
 * Created by hungpham on 8/21/2025
 */
import java.util.List;
import java.util.Map;

public record GeminiRequest(
        Map<String, Object> system_instruction,
        List<Map<String, Object>> contents
) {
    /**
     * Tạo request từ systemPrompt + full history (user/model).
     */
    public static GeminiRequest fromHistory(String systemPrompt, List<Map<String, Object>> history) {
        Map<String, Object> system = Map.of(
                "role", "system",
                "parts", List.of(Map.of("text", systemPrompt))
        );
        return new GeminiRequest(system, history);
    }

    /**
     * Build 1 message (user hoặc model).
     */
    public static Map<String, Object> msg(String role, String text) {
        return Map.of(
                "role", role,
                "parts", List.of(Map.of("text", text))
        );
    }
}
