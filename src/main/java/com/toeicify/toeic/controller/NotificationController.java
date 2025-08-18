package com.toeicify.toeic.controller;

import com.toeicify.toeic.dto.request.notification.SendNotificationRequest;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.entity.Notification;
import com.toeicify.toeic.service.NotificationService;
import com.toeicify.toeic.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by hungpham on 8/18/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/register-token")
    public ResponseEntity<String> registerToken(@RequestBody Map<String, String> body) {
        Long userId = Long.parseLong(body.get("userId"));
        String token = body.get("token");

        notificationService.registerToken(userId, token);
        return ResponseEntity.ok("Token saved");
    }

    @GetMapping
    public ResponseEntity<PaginationResponse> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getNotificationsByUser(page, size));
    }

    @PostMapping("/mark-read/{id}")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<String> markAllAsRead() {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok("Notification deleted");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAllNotifications() {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.deleteAllNotifications(userId);
        return ResponseEntity.ok("All notifications deleted");
    }
}
