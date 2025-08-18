package com.toeicify.toeic.service;

import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.entity.Notification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by hungpham on 8/18/2025
 */
public interface NotificationService {

    @Async
    void saveNotification(Long userId, String title, String content);

    @Async
    void sendNotification(Long userId, String title, String content);

    @Transactional
    void registerToken(Long userId, String token);

    PaginationResponse getNotificationsByUser(int page, int size);

    @Async
    void markAsRead(Long notificationId);

    @Async
    @Transactional
    void markAllAsRead(Long userId);

    @Async
    @Transactional
    void deleteNotification(Long notificationId, Long userId);

    @Async
    @Transactional
    void deleteAllNotifications(Long userId);
}
