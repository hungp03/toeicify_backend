package com.toeicify.toeic.mapper;

/**
 * Created by hungpham on 8/31/2025
 */
import com.toeicify.toeic.dto.response.schedule.TodoResponse;
import com.toeicify.toeic.entity.Todo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TodoMapper {
    TodoResponse toDto(Todo entity);
}
