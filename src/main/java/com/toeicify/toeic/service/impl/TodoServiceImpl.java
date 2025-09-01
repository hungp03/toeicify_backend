package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.entity.Todo;
import com.toeicify.toeic.exception.AccessDeniedException;
import com.toeicify.toeic.exception.ResourceNotFoundException;
import com.toeicify.toeic.repository.TodoRepository;
import com.toeicify.toeic.service.TodoService;
import com.toeicify.toeic.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Created by hungpham on 9/1/2025
 */
@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {
    private final TodoRepository todoRepository;
    @Override
    @Transactional
    public void setCompleted(Long todoId, boolean completed) {
        Long userId = SecurityUtil.getCurrentUserId();
        int updated = todoRepository.updateCompletionIfOwner(todoId, userId, completed);
        if (updated == 0) {
            throw new AccessDeniedException("Todo not found or you don't have permission to change this task");
        }
    }

    @Override
    public void quickUpdate(Long todoId, String description, LocalDateTime dueDate, Boolean completed) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long ownerId = todoRepository.findOwnerIdByTodoId(todoId);
        if (ownerId == null) {
            throw new ResourceNotFoundException("Todo not found: " + todoId);
        }
        if (!ownerId.equals(userId)) {
            throw new AccessDeniedException("You don't have permission to change this task");
        }

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found: " + todoId));

        if (description != null) todo.setTaskDescription(description);
        if (dueDate != null) todo.setDueDate(dueDate);
        if (completed != null) todo.setIsCompleted(completed);

        todoRepository.save(todo);
    }

    @Override
    public void quickDelete(Long todoId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long ownerId = todoRepository.findOwnerIdByTodoId(todoId);
        if (ownerId == null) {
            throw new ResourceNotFoundException("Todo not found: " + todoId);
        }
        if (!ownerId.equals(userId)) {
            throw new AccessDeniedException("You don't have permission to delete this task");
        }

        todoRepository.deleteById(todoId);
    }
}
