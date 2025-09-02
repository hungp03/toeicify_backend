package com.toeicify.toeic.service;

import com.toeicify.toeic.dto.request.schedule.CreateStudyScheduleRequest;
import com.toeicify.toeic.dto.request.schedule.UpdateStudyScheduleRequest;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.schedule.StudyScheduleResponse;
import org.springframework.data.domain.Pageable;

/**
 * Created by hungpham on 8/31/2025
 */
public interface StudyScheduleService {
    StudyScheduleResponse create(CreateStudyScheduleRequest req);
    StudyScheduleResponse update(Long scheduleId, UpdateStudyScheduleRequest req);
    void delete(Long scheduleId);
    PaginationResponse getSchedulesByUser(Pageable pageable);
}
