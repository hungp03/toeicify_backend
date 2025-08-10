package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.dto.response.admin.AdminDashboardResponse;
import com.toeicify.toeic.entity.Exam;
import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.entity.UserAttempt;
import com.toeicify.toeic.repository.ExamRepository;
import com.toeicify.toeic.repository.QuestionRepository;
import com.toeicify.toeic.repository.UserAttemptRepository;
import com.toeicify.toeic.repository.UserRepository;
import com.toeicify.toeic.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final UserAttemptRepository userAttemptRepository;

    @Override
    public AdminDashboardResponse getDashboardData() {

        long totalUsers = userRepository.count();
        long totalExams = examRepository.count();
        long totalQuestions = questionRepository.count();

        LocalDate now = LocalDate.now();
        LocalDate firstOfThisMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate firstOfLastMonth = firstOfThisMonth.minusMonths(1);
        long usersThisMonth = userRepository.countByRegistrationDateAfter(firstOfThisMonth.atStartOfDay(ZoneOffset.UTC).toInstant());
        long usersLastMonth = userRepository.countByRegistrationDateBetween(firstOfLastMonth.atStartOfDay(ZoneOffset.UTC).toInstant(), firstOfThisMonth.atStartOfDay(ZoneOffset.UTC).toInstant());
        String growth = "";
        if(usersThisMonth==0 && usersLastMonth==0){
            growth ="+0";
        }
        else if(usersLastMonth==0 && usersThisMonth != 0){
            growth = "+100";
        }
        else if(usersThisMonth==0){
            growth = "+0%";
        } else {
            growth = usersLastMonth > 0 ? "+" + ((usersThisMonth / usersLastMonth)) + "%" : "+0%";
        }
        List<AdminDashboardResponse.StatItem> stats = List.of(
                AdminDashboardResponse.StatItem.builder().title("Tổng người dùng").value(String.valueOf(totalUsers)).change("").color("text-blue-600").build(),
                AdminDashboardResponse.StatItem.builder().title("Số đề thi").value(String.valueOf(totalExams)).change("").color("text-green-600").build(),
                AdminDashboardResponse.StatItem.builder().title("Số câu hỏi").value(String.valueOf(totalQuestions)).change("").color("text-purple-600").build(),
                AdminDashboardResponse.StatItem.builder().title("Tăng trưởng").value(growth.replace("+", "")).change("").color("text-orange-600").build()
        );

        List<AdminDashboardResponse.MonthlyDataItem> monthlyData = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
            long users = userRepository.countByRegistrationDateBetween(monthStart.atStartOfDay(ZoneOffset.UTC).toInstant(), monthEnd.atStartOfDay(ZoneOffset.UTC).toInstant().plus(1, ChronoUnit.DAYS));
            long tests = examRepository.countByCreatedAtBetween(monthStart.atStartOfDay(ZoneOffset.UTC).toInstant(), monthEnd.atStartOfDay(ZoneOffset.UTC).toInstant().plus(1, ChronoUnit.DAYS));
            monthlyData.add(AdminDashboardResponse.MonthlyDataItem.builder().name(monthStart.format(monthFormatter)).users(users).tests(tests).build());
        }

        List<AdminDashboardResponse.WeeklyGrowthItem> weeklyGrowth = new ArrayList<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = now.minusDays(i);
            long value = userRepository.countByRegistrationDateBetween(day.atStartOfDay(ZoneOffset.UTC).toInstant(), day.atStartOfDay(ZoneOffset.UTC).toInstant().plus(1, ChronoUnit.DAYS));
            weeklyGrowth.add(AdminDashboardResponse.WeeklyGrowthItem.builder().name(day.format(dayFormatter)).value(value).build());
        }

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
                        .action("Người dùng hoàn thành bài thi")
                        .user(a.getUser().getUsername())
                        .time(durationToString(a.getEndTime()))
                        .build())
                .toList());
        activities = activities.stream().sorted(Comparator.comparing(a -> parseTime(a.getTime()))).limit(4).toList(); // Sort và limit 4

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