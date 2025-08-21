package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.dto.request.gemini.ChatMessage;
import com.toeicify.toeic.dto.request.gemini.GeminiRequest;
import com.toeicify.toeic.dto.response.gemini.GeminiResponse;
import com.toeicify.toeic.service.ChatService;
import com.toeicify.toeic.util.ChatContextStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hungpham on 8/21/2025
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final WebClient webClient;
    private final ChatContextStore store;
    @Value("${gemini.api.model}")
    private String model;
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.system-prompt}")
    private String SYSTEM_PROMPT;

    @Override
    public Flux<GeminiResponse> chat(String sessionId, String prompt) {
        // 1) Lưu user message vào Redis
        store.append(sessionId, ChatMessage.user(prompt));

        // 2) Lấy history (bao gồm user vừa gửi)
        List<ChatMessage> history = store.getHistory(sessionId);

        // 3) Build request Gemini từ history
        List<Map<String, Object>> contents = new ArrayList<>();
        for (ChatMessage m : history) {
            contents.add(GeminiRequest.msg(m.role(), m.text()));
        }
        GeminiRequest request = GeminiRequest.fromHistory(SYSTEM_PROMPT, contents);

        // 4) Gọi Gemini stream API
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/{model}:streamGenerateContent")
                        .queryParam("key", apiKey)
                        .build(model))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(GeminiResponse.class)
                .transform(resFlux -> persistModelAnswerAtEnd(sessionId, resFlux));
    }

    /** Gom text từ stream, cuối stream lưu 1 message model vào Redis */
    private Flux<GeminiResponse> persistModelAnswerAtEnd(String sessionId, Flux<GeminiResponse> source) {
        final StringBuilder sb = new StringBuilder();
        return source
                .doOnNext(res -> {
                    String t = res.firstText();
                    if (t != null) sb.append(t);
                })
                .doOnComplete(() -> {
                    String finalText = sb.toString();
                    if (!finalText.isEmpty()) {
                        store.append(sessionId, ChatMessage.model(finalText));
                    }
                });
    }
}
