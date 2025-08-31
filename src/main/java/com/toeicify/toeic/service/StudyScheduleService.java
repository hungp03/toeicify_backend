package com.toeicify.toeic.service;

import com.toeicify.toeic.dto.request.schedule.CreateStudyScheduleRequest;
import com.toeicify.toeic.dto.request.schedule.UpdateStudyScheduleRequest;
import com.toeicify.toeic.dto.response.schedule.StudyScheduleResponse;

/**
 * Created by hungpham on 8/31/2025
 */
public interface StudyScheduleService {
    StudyScheduleResponse create(CreateStudyScheduleRequest req);
    StudyScheduleResponse update(Long scheduleId, UpdateStudyScheduleRequest req);
}
