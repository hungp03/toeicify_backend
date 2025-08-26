package com.toeicify.toeic.controller;

import com.toeicify.toeic.dto.request.gemini.ChatRequest;
import com.toeicify.toeic.dto.response.gemini.GeminiResponse;
import com.toeicify.toeic.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.UUID;

/**
 * Created by hungpham on 8/21/2025
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<GeminiResponse> chat(@Valid @RequestBody ChatRequest request) {
        String sessionId = request.sessionId() != null ? request.sessionId() : UUID.randomUUID().toString();
        return chatService.chat(sessionId, request.prompt());
    }
}

