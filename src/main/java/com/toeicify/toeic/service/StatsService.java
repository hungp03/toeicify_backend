package com.toeicify.toeic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.toeicify.toeic.dto.response.stats.AdminDashboardResponse;
import com.toeicify.toeic.dto.response.stats.UserProgressResponse;

/**
 * Created by hungpham on 8/11/2025
 */
public interface StatsService {
    UserProgressResponse getUserProgress(int chartLimit) throws JsonProcessingException;

    AdminDashboardResponse getDashboardData();
}
