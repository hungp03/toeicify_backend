package com.toeicify.toeic.mapper;

import com.toeicify.toeic.dto.response.schedule.StudyScheduleResponse;
import com.toeicify.toeic.entity.StudySchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Created by hungpham on 8/31/2025
 */
@Mapper(componentModel = "spring", uses = {TodoMapper.class})
public interface StudyScheduleMapper {

    @Mapping(source = "user.userId", target = "userId")
    StudyScheduleResponse toDto(StudySchedule entity);
}
