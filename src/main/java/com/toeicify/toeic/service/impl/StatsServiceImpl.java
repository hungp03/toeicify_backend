package com.toeicify.toeic.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeicify.toeic.dto.response.stats.AdminDashboardResponse;
import com.toeicify.toeic.dto.response.stats.UserProgressResponse;
import com.toeicify.toeic.entity.Exam;
import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.entity.UserAttempt;
import com.toeicify.toeic.repository.ExamRepository;
import com.toeicify.toeic.repository.QuestionRepository;
import com.toeicify.toeic.repository.UserAttemptRepository;
import com.toeicify.toeic.repository.UserRepository;
import com.toeicify.toeic.service.StatsService;
import com.toeicify.toeic.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by hungpham on 8/11/2025
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final UserAttemptRepository userAttemptRepository;
    private final ObjectMapper objectMapper;

    @Override
    public UserProgressResponse getUserProgress(int chartLimit) throws JsonProcessingException {
        Long userId = SecurityUtil.getCurrentUserId();
        String json = userAttemptRepository.getUserProgress(userId, chartLimit);
        JsonNode root = objectMapper.readTree(json);

        UserProgressResponse.Summary summary = new UserProgressResponse.Summary(
                root.path("summary").path("currentScore").asInt(0),
                root.path("summary").path("testsTaken").asInt(0),
                new BigDecimal(root.path("summary").path("studyHours").asText("0"))
        );

        UserProgressResponse.SectionHighs highs = new UserProgressResponse.SectionHighs(
                root.path("sectionHighs").path("listeningMax").asInt(0),
                root.path("sectionHighs").path("readingMax").asInt(0)
        );

        List<UserProgressResponse.TrendPoint> trend = new ArrayList<>();
        for (JsonNode n : root.path("scoreTrend")) {
            trend.add(new UserProgressResponse.TrendPoint(n.path("day").asText(), n.path("score").asInt()));
        }

        return new UserProgressResponse(summary, highs, trend);
    }

    @Override
    public AdminDashboardResponse getDashboardData() {
        // Tổng số liệu
        long totalUsers = userRepository.count();
        long totalExams = examRepository.count();
        long totalQuestions = questionRepository.count();

        // Tính toán tăng trưởng
        LocalDate now = LocalDate.now();
        LocalDate firstOfThisMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate firstOfLastMonth = firstOfThisMonth.minusMonths(1);
        long usersThisMonth = userRepository.countByRegistrationDateAfter(firstOfThisMonth.atStartOfDay(ZoneOffset.UTC).toInstant());
        long usersLastMonth = userRepository.countByRegistrationDateBetween(firstOfLastMonth.atStartOfDay(ZoneOffset.UTC).toInstant(), firstOfThisMonth.atStartOfDay(ZoneOffset.UTC).toInstant());
        String growth;
        if (usersThisMonth == 0 && usersLastMonth == 0) {
            growth = "+0%";
        } else if (usersLastMonth == 0) {
            growth = "+100%";
        } else if (usersThisMonth == 0) {
            growth = "-100%";
        } else {
            BigDecimal growthRate = BigDecimal.valueOf(usersThisMonth)
                    .divide(BigDecimal.valueOf(usersLastMonth), 2, RoundingMode.HALF_UP)
                    .subtract(BigDecimal.ONE)
                    .multiply(BigDecimal.valueOf(100));
            growth = (growthRate.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + growthRate.setScale(0, RoundingMode.HALF_UP) + "%";
        }

        List<AdminDashboardResponse.StatItem> stats = List.of(
                AdminDashboardResponse.StatItem.builder().title("Tổng người dùng").value(String.valueOf(totalUsers)).build(),
                AdminDashboardResponse.StatItem.builder().title("Số đề thi").value(String.valueOf(totalExams)).build(),
                AdminDashboardResponse.StatItem.builder().title("Số câu hỏi").value(String.valueOf(totalQuestions)).build(),
                AdminDashboardResponse.StatItem.builder().title("Tăng trưởng").value(growth.replace("+", "")).build()
        );

        // Tối ưu monthlyData
        List<AdminDashboardResponse.MonthlyDataItem> monthlyData = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");
        LocalDate start = now.minusMonths(6).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = now.with(TemporalAdjusters.lastDayOfMonth());

        // Lấy dữ liệu users theo tháng
        List<Object[]> userCounts = userRepository.countUsersByMonth(
                start.atStartOfDay(ZoneOffset.UTC).toInstant(),
                end.atStartOfDay(ZoneOffset.UTC).toInstant().plus(1, ChronoUnit.DAYS)
        );

        // Lấy dữ liệu exams theo tháng
        List<Object[]> examCounts = examRepository.countExamsByMonth(
                start.atStartOfDay(ZoneOffset.UTC).toInstant(),
                end.atStartOfDay(ZoneOffset.UTC).toInstant().plus(1, ChronoUnit.DAYS)
        );

        // Gộp dữ liệu vào monthlyData
        Map<String, Long> userMap = userCounts.stream()
                .collect(Collectors.toMap(
                        arr -> {
                            Object dateObj = arr[0];
                            LocalDateTime dateTime;
                            if (dateObj instanceof Timestamp) {
                                dateTime = ((Timestamp) dateObj).toLocalDateTime();
                            } else if (dateObj instanceof Instant) {
                                dateTime = LocalDateTime.ofInstant((Instant) dateObj, ZoneOffset.UTC);
                            } else {
                                throw new IllegalStateException("Unsupported date type: " + dateObj.getClass());
                            }
                            return dateTime.format(monthFormatter);
                        },
                        arr -> (Long) arr[1],
                        (v1, v2) -> v1
                ));

        Map<String, Long> examMap = examCounts.stream()
                .collect(Collectors.toMap(
                        arr -> {
                            Object dateObj = arr[0];
                            LocalDateTime dateTime;
                            if (dateObj instanceof Timestamp) {
                                dateTime = ((Timestamp) dateObj).toLocalDateTime();
                            } else if (dateObj instanceof Instant) {
                                dateTime = LocalDateTime.ofInstant((Instant) dateObj, ZoneOffset.UTC);
                            } else {
                                throw new IllegalStateException("Unsupported date type: " + dateObj.getClass());
                            }
                            return dateTime.format(monthFormatter);
                        },
                        arr -> (Long) arr[1],
                        (v1, v2) -> v1
                ));

        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthName = month.format(monthFormatter);
            long users = userMap.getOrDefault(monthName, 0L);
            long tests = examMap.getOrDefault(monthName, 0L);
            monthlyData.add(AdminDashboardResponse.MonthlyDataItem.builder()
                    .name(monthName)
                    .users(users)
                    .tests(tests)
                    .build());
        }

        // Tối ưu weeklyGrowth
        List<AdminDashboardResponse.WeeklyGrowthItem> weeklyGrowth = new ArrayList<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
        start = now.minusDays(7);
        end = now;

        List<Object[]> dailyUserCounts = userRepository.countUsersByDay(
                start.atStartOfDay(ZoneOffset.UTC).toInstant(),
                end.atStartOfDay(ZoneOffset.UTC).toInstant().plus(1, ChronoUnit.DAYS)
        );

        Map<String, Long> dailyUserMap = dailyUserCounts.stream()
                .collect(Collectors.toMap(
                        arr -> {
                            Object dateObj = arr[0];
                            LocalDateTime dateTime;
                            if (dateObj instanceof Timestamp) {
                                dateTime = ((Timestamp) dateObj).toLocalDateTime();
                            } else if (dateObj instanceof Instant) {
                                dateTime = LocalDateTime.ofInstant((Instant) dateObj, ZoneOffset.UTC);
                            } else {
                                throw new IllegalStateException("Unsupported date type: " + dateObj.getClass());
                            }
                            return dateTime.format(dayFormatter);
                        },
                        arr -> (Long) arr[1],
                        (v1, v2) -> v1
                ));

        for (int i = 6; i >= 0; i--) {
            LocalDate day = now.minusDays(i);
            String dayName = day.format(dayFormatter);
            long value = dailyUserMap.getOrDefault(dayName, 0L);
            weeklyGrowth.add(AdminDashboardResponse.WeeklyGrowthItem.builder()
                    .name(dayName)
                    .value(value)
                    .build());
        }

        // Hoạt động gần đây
        List<User> recentUsers = userRepository.findTop1ByOrderByRegistrationDateDesc();
        List<AdminDashboardResponse.ActivityItem> activities = recentUsers.stream()
                .map(u -> AdminDashboardResponse.ActivityItem.builder()
                        .action("Người dùng mới đăng ký")
                        .user(u.getEmail() != null ? u.getEmail() : u.getUsername())
                        .time(durationToString(u.getRegistrationDate()))
                        .build())
                .collect(Collectors.toList());

        List<Exam> recentExams = examRepository.findTop1ByOrderByCreatedAtDesc();
        activities.addAll(recentExams.stream()
                .map(e -> AdminDashboardResponse.ActivityItem.builder()
                        .action("Đề thi mới được tạo bởi")
                        .user(e.getCreatedBy().getUsername())
                        .time(durationToString(e.getCreatedAt()))
                        .build())
                .toList());

        List<UserAttempt> recentUserAttempt = userAttemptRepository.findTop1ByOrderByEndTimeDesc();
        activities.addAll(recentUserAttempt.stream()
                .map(a -> AdminDashboardResponse.ActivityItem.builder()
                        .action("Người dùng hoàn thành bài thi mới nhất")
                        .user(a.getUser().getUsername())
                        .time(durationToString(a.getEndTime()))
                        .build())
                .toList());

        activities = activities.stream()
                .sorted(Comparator.comparing(a -> parseTime(a.time())))
                .limit(4)
                .toList();

        return AdminDashboardResponse.builder()
                .stats(stats)
                .monthlyData(monthlyData)
                .weeklyGrowth(weeklyGrowth)
                .recentActivities(activities)
                .build();
    }

    private String durationToString(Instant time) {
        Duration duration = Duration.between(time, Instant.now());
        if (duration.toMinutes() < 60) return duration.toMinutes() + " phút trước";
        if (duration.toHours() < 24) return duration.toHours() + " giờ trước";
        return duration.toDays() + " ngày trước";
    }

    private Instant parseTime(String timeStr) {
        return Instant.now();
    }
}