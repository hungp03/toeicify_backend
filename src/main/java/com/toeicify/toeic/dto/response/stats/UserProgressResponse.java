package com.toeicify.toeic.dto.response.stats;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by hungpham on 8/10/2025
 */

public record UserProgressResponse (
        Summary summary,
        SectionHighs sectionHighs,
        List<TrendPoint> scoreTrend
) {
    public record TrendPoint(String day, Integer score) {}
    public record Summary(Integer currentScore, Integer testsTaken, BigDecimal studyHours) {}
    public record SectionHighs(Integer listeningMax, Integer readingMax) {}
}