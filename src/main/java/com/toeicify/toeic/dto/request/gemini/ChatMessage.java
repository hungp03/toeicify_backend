package com.toeicify.toeic.dto.request.gemini;

/**
 * Created by hungpham on 8/21/2025
 */
public record ChatMessage(String role, String text, long ts) {
    public static ChatMessage user(String text) { return new ChatMessage("user", text, System.currentTimeMillis()); }
    public static ChatMessage model(String text) { return new ChatMessage("model", text, System.currentTimeMillis()); }
}