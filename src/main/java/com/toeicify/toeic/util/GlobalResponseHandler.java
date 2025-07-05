package com.toeicify.toeic.util;

import com.nimbusds.jose.util.Resource;
import com.toeicify.toeic.dto.response.ApiResponse;
import com.toeicify.toeic.util.annotation.ApiMessage;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof String || body instanceof Resource || body instanceof byte[] || body instanceof ApiResponse<?>) {
            return body;
        }
        String message = "Call api successful!";

        ApiMessage apiMessage = returnType.getMethodAnnotation(ApiMessage.class);
        if (apiMessage != null) {
            message = apiMessage.value();
        }
        return new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                body,
                message,
                null
        );
    }
}
