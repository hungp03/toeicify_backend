package com.toeicify.toeic.dto.response.stats;

import lombok.Builder;
import java.util.List;

@Builder
public record AdminDashboardResponse(
        List<StatItem> stats,
        List<MonthlyDataItem> monthlyData,
        List<WeeklyGrowthItem> weeklyGrowth,
        List<ActivityItem> recentActivities
) {
    @Builder
    public record StatItem(
            String title,
            String value
    ) {}

    @Builder
    public record MonthlyDataItem(
            String name,
            long users,
            long tests
    ) {}

    @Builder
    public record WeeklyGrowthItem(
            String name,
            long value
    ) {}

    @Builder
    public record ActivityItem(
            String action,
            String user,
            String time
    ) {}
}
