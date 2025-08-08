package com.toeicify.toeic.util;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by hungpham on 8/8/2025
 */
@Component("toeicPartKeyGenerator")
public class ToeicPartKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        List<Long> partIds = (List<Long>) params[0];

        if (partIds == null || partIds.isEmpty()) {
            return "toeicPart:empty";
        }

        // Sort để đảm bảo [1,2,3] và [3,1,2] có cùng cache key
        String sortedPartIds = partIds.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining("-"));

        return "toeicPart:" + sortedPartIds;
    }
}