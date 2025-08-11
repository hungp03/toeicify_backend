package com.toeicify.toeic.dto.response.stats;

import java.util.List;

/**
 * Created by hungpham on 8/10/2025
 */
public record ChartPracticePointData (List<String> labels, List<Integer> data) {}