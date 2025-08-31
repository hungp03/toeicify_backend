package com.toeicify.toeic.controller;

import com.toeicify.toeic.dto.request.schedule.CreateStudyScheduleRequest;
import com.toeicify.toeic.dto.request.schedule.UpdateStudyScheduleRequest;
import com.toeicify.toeic.dto.response.schedule.StudyScheduleResponse;
import com.toeicify.toeic.service.StudyScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hungpham on 8/31/2025
 */
@RestController
@RequestMapping("api/study-schedule")
@RequiredArgsConstructor
public class StudyScheduleController {
    private final StudyScheduleService studyScheduleService;

    @PostMapping
    public ResponseEntity<StudyScheduleResponse> create(@RequestBody @Valid CreateStudyScheduleRequest req) {
        return ResponseEntity.ok(studyScheduleService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyScheduleResponse> update(
            @PathVariable("id") Long id,
            @RequestBody UpdateStudyScheduleRequest req) {
        return ResponseEntity.ok(studyScheduleService.update(id, req));
    }
}
