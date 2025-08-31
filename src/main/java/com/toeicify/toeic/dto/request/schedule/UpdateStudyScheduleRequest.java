package com.toeicify.toeic.dto.request.schedule;

/**
 * Created by hungpham on 8/31/2025
 */
import java.util.List;

public record UpdateStudyScheduleRequest(
        String title,
        String description,
        List<UpdateTodoRequest> todos
) {}
