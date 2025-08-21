package com.toeicify.toeic.service;

import com.toeicify.toeic.dto.response.gemini.GeminiResponse;
import reactor.core.publisher.Flux;

/**
 * Created by hungpham on 8/21/2025
 */
public interface ChatService {
    Flux<GeminiResponse> chat(String sessionId, String prompt);
}
