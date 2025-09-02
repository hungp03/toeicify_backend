package com.toeicify.toeic.mapper;

/**
 * Created by hungpham on 8/31/2025
 */
import com.toeicify.toeic.dto.response.schedule.ScheduleTodoResponse;
import com.toeicify.toeic.dto.response.schedule.TodoResponse;
import com.toeicify.toeic.entity.Todo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TodoMapper {
    ScheduleTodoResponse toDto(Todo entity);
    @Mapping(source = "schedule.scheduleId", target = "scheduleId")
    TodoResponse toTodoResponse (Todo todo);
}
