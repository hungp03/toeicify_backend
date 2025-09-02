package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.dto.request.schedule.CreateStudyScheduleRequest;
import com.toeicify.toeic.dto.request.schedule.UpdateStudyScheduleRequest;
import com.toeicify.toeic.dto.request.schedule.UpdateTodoRequest;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.schedule.StudyScheduleResponse;
import com.toeicify.toeic.entity.StudySchedule;
import com.toeicify.toeic.entity.Todo;
import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.exception.AccessDeniedException;
import com.toeicify.toeic.exception.ResourceNotFoundException;
import com.toeicify.toeic.mapper.StudyScheduleMapper;
import com.toeicify.toeic.repository.StudyScheduleRepository;
import com.toeicify.toeic.service.StudyScheduleService;
import com.toeicify.toeic.service.UserService;
import com.toeicify.toeic.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hungpham on 8/31/2025
 */
@Service
@RequiredArgsConstructor
public class StudyScheduleServiceImpl implements StudyScheduleService {
    private final StudyScheduleRepository scheduleRepository;
    private final UserService userService;
    private final StudyScheduleMapper mapper;
    @Override
    public StudyScheduleResponse create(CreateStudyScheduleRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userService.findById(userId);

        StudySchedule schedule = StudySchedule.builder()
                .title(req.title())
                .description(req.description())
                .user(user)
                .build();

        if (req.todos() != null && !req.todos().isEmpty()) {
            List<Todo> todoEntities = req.todos().stream().map(t -> Todo.builder()
                    .taskDescription(t.taskDescription())
                    .isCompleted(false)
                    .dueDate(t.dueDate())
                    .schedule(schedule)
                    .build()).toList();
            schedule.setTodos(todoEntities);
        }

        StudySchedule saved = scheduleRepository.save(schedule);
        return mapper.toDto(saved);
    }

    @Override
    public StudyScheduleResponse update(Long scheduleId, UpdateStudyScheduleRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long ownerId = scheduleRepository.findOwnerIdByScheduleId(scheduleId);
        if (ownerId == null) {
            throw new ResourceNotFoundException("Schedule not found: " + scheduleId);
        }
        if (!ownerId.equals(userId)) {
            throw new AccessDeniedException("You do not have permission to update this study schedule");
        }

        StudySchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found: " + scheduleId));
        if (req.title() != null) schedule.setTitle(req.title());
        if (req.description() != null) schedule.setDescription(req.description());
        Map<Long, Todo> currentTodos = new HashMap<>();
        if (schedule.getTodos() != null) {
            for (Todo t : schedule.getTodos()) {
                currentTodos.put(t.getTodoId(), t);
            }
        }
        List<Todo> newList = new ArrayList<>();

        if (req.todos() != null) {
            for (UpdateTodoRequest dto : req.todos()) {
                if (dto.todoId() != null && currentTodos.containsKey(dto.todoId())) {
                    // update old todo
                    Todo existing = currentTodos.get(dto.todoId());
                    existing.setTaskDescription(dto.taskDescription());
                    existing.setIsCompleted(dto.isCompleted() != null ? dto.isCompleted() : existing.getIsCompleted());
                    existing.setDueDate(dto.dueDate());
                    newList.add(existing);
                } else {
                    // thêm todo mới
                    Todo newTodo = Todo.builder()
                            .taskDescription(dto.taskDescription())
                            .isCompleted(dto.isCompleted() != null ? dto.isCompleted() : false)
                            .dueDate(dto.dueDate())
                            .schedule(schedule)
                            .build();
                    newList.add(newTodo);
                }
            }
        }

        // orphanRemoval = true -> Any todo that is no longer in the newList will be automatically deleted.
        schedule.getTodos().clear();
        schedule.getTodos().addAll(newList);

        StudySchedule saved = scheduleRepository.save(schedule);
        return mapper.toDto(saved);
    }

    @Override
    public void delete(Long scheduleId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long ownerId = scheduleRepository.findOwnerIdByScheduleId(scheduleId);
        if (ownerId == null) {
            throw new ResourceNotFoundException("Schedule not found: " + scheduleId);
        }
        if (!ownerId.equals(userId)) {
            throw new AccessDeniedException("You do not have permission to delete this study schedule");
        }
        scheduleRepository.deleteById(scheduleId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse getSchedulesByUser(Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<StudySchedule> studySchedulePage = scheduleRepository.findByUser_UserId(userId, pageable);
        return PaginationResponse.from(studySchedulePage.map(mapper::toDto), pageable);
    }
}
