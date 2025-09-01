package com.toeicify.toeic.controller;

import com.toeicify.toeic.dto.request.schedule.QuickUpdateTodoRequest;
import com.toeicify.toeic.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hungpham on 9/1/2025
 */
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;
    @PatchMapping("/{id}/completion")
    public ResponseEntity<Void> setCompletion(
            @PathVariable("id") Long id,
            @RequestParam("completed") boolean completed
    ) {
        todoService.setCompleted(id, completed);
        return ResponseEntity.noContent().build(); // 204
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> quickUpdate(
            @PathVariable("id") Long id,
            @RequestBody QuickUpdateTodoRequest req
    ) {
        todoService.quickUpdate(id, req.taskDescription(), req.dueDate(), req.isCompleted());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> quickDelete(
            @PathVariable("id") Long id
    ) {
        todoService.quickDelete(id);
        return ResponseEntity.noContent().build();
    }
}
