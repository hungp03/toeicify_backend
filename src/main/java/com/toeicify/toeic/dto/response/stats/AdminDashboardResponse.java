package com.toeicify.toeic.dto.response.stats;

/**
 * Created by hungpham on 8/11/2025
 */
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminDashboardResponse {
    private List<StatItem> stats;
    private List<MonthlyDataItem> monthlyData;
    private List<WeeklyGrowthItem> weeklyGrowth;
    private List<ActivityItem> recentActivities;

    @Data
    @Builder
    public static class StatItem {
        private String title;
        private String value;
    }

    @Data
    @Builder
    public static class MonthlyDataItem {
        private String name;
        private long users;
        private long tests;
    }

    @Data
    @Builder
    public static class WeeklyGrowthItem {
        private String name;
        private long value;
    }

    @Data
    @Builder
    public static class ActivityItem {
        private String action;
        private String user;
        private String time;
    }
}
