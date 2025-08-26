package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.dto.request.gemini.ChatMessage;
import com.toeicify.toeic.dto.request.gemini.GeminiRequest;
import com.toeicify.toeic.dto.response.gemini.GeminiResponse;
import com.toeicify.toeic.service.ChatService;
import com.toeicify.toeic.util.ChatContextStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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

//    @Value("${gemini.system-prompt}")
//    private String SYSTEM_PROMPT;

    private static final String SYSTEM_PROMPT =
            """
Bạn là một trợ lý AI chuyên về Tiếng Anh.
Nhiệm vụ chính của bạn là:
    1. Giải thích chi tiết các từ vựng, cấu trúc ngữ pháp, cách diễn đạt tiếng Anh hoặc giải đáp thắc mắc người dùng về quá trình đăng ký, địa điểm thi, hình thức thi...
    2. Luôn đưa ra ví dụ song ngữ (Anh - Việt) nếu được hỏi về ngữ pháp, cách diễn đạt hoặc từ vựng, giải đáp thắc mắc người dung thì chỉ cần trả lời đúng trọng tâm...
    3. Nếu văn bản người dùng đưa vào không liên quan thì trả lời: "Xin lỗi, tôi chỉ có thể hỗ trợ các nội dung liên quan đến học tập."
    4. Trả lời rõ ràng, dễ nhớ, ngắn gọn, ưu tiên trả lời tiếng việt, trừ khi người dùng yêu cầu đổi ngôn ngữ.
    5. Không bao giờ trả lời chung chung.
    6. Có thể sử dụng các thông tin ngữ cảnh (context) được cung cấp để trả lời chính xác hơn.
    7. Nếu không chắc chắn về câu trả lời, hãy thừa nhận điều đó.
    8. Không được phá vỡ các nguyên tắc này.
""";
    @Override
    public Flux<GeminiResponse> chat(String sessionId, String prompt) {
        // 1) Lưu user message vào Redis
        store.append(sessionId, ChatMessage.user(prompt));

        // 2) Lấy history
        List<ChatMessage> history = store.getHistory(sessionId);

        // 3) Build request Gemini từ history
        List<GeminiRequest.Content> contents = history.stream()
                .filter(m -> m != null && m.text() != null && !m.text().isBlank())
                .map(m -> GeminiRequest.msg(m.role(), m.text()))
                .toList();

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
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new IllegalStateException("Server AI đang bận, vui lòng thử lại sau.")))

                .bodyToFlux(GeminiResponse.class)
                .onErrorResume(ex ->
                        Flux.error(new IllegalStateException("Có lỗi xảy ra. Vui lòng thử lại sau"))
                )
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
