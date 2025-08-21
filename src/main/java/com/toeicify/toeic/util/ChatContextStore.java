package com.toeicify.toeic.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeicify.toeic.dto.request.gemini.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by hungpham on 8/21/2025
 */
@Component
@RequiredArgsConstructor
public class ChatContextStore {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${chat.history.max-messages:10}")
    private int maxMessages;

    @Value("${chat.history.ttl-seconds:3600}")
    private long ttlSeconds;

    private String key(String sessionId) {
        return "chat:ctx:" + sessionId;
    }

    private String toJson(ChatMessage m) {
        try {
            return mapper.writeValueAsString(m);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private ChatMessage fromJson(String s) {
        try {
            return mapper.readValue(s, ChatMessage.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Lưu message mới nhất vào list, cắt bớt để giữ maxMessages, đặt TTL */
    public void append(String sessionId, ChatMessage message) {
        String k = key(sessionId);

        // push vào head
        redisTemplate.opsForList().leftPush(k, toJson(message));

        // trim để giữ maxMessages
        redisTemplate.opsForList().trim(k, 0, maxMessages - 1);

        // reset TTL
        redisTemplate.expire(k, Duration.ofSeconds(ttlSeconds));
    }

    /** Lấy lịch sử từ cũ -> mới */
    public List<ChatMessage> getHistory(String sessionId) {
        String k = key(sessionId);
        List<Object> list = redisTemplate.opsForList().range(k, 0, maxMessages - 1);

        if (list == null) return Collections.emptyList();

        List<ChatMessage> messages = list.stream()
                .map(o -> fromJson(o.toString()))
                .collect(Collectors.toList());

        // redis trả về từ mới -> cũ => đảo lại
        Collections.reverse(messages);
        return messages;
    }

    public void clear(String sessionId) {
        redisTemplate.delete(key(sessionId));
    }
}