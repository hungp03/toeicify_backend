package com.toeicify.toeic.service;

import com.toeicify.toeic.dto.request.schedule.CreateSingleTodoRequest;
import com.toeicify.toeic.dto.response.schedule.ScheduleTodoResponse;
import com.toeicify.toeic.dto.response.schedule.TodoResponse;

import java.time.LocalDateTime;

/**
 * Created by hungpham on 9/1/2025
 */
public interface TodoService {
    TodoResponse createTodo(CreateSingleTodoRequest req);
    void setCompleted(Long todoId, boolean completed);
    TodoResponse quickUpdate(Long todoId, String description, LocalDateTime dueDate);
    void quickDelete(Long todoId);
}
