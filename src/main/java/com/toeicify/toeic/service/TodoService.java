package com.toeicify.toeic.service;

import java.time.LocalDateTime;

/**
 * Created by hungpham on 9/1/2025
 */
public interface TodoService {
    void setCompleted(Long todoId, boolean completed);
    void quickUpdate(Long todoId, String description, LocalDateTime dueDate, Boolean completed);
    void quickDelete(Long todoId);
}
