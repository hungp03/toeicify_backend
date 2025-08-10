package com.toeicify.toeic.dto.response.stats;

import java.time.LocalDate;

/**
 * Created by hungpham on 8/10/2025
 */
public record PracticePointResponse(LocalDate day, Integer score) {}