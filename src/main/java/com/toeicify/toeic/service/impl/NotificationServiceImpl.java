package com.toeicify.toeic.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.entity.Notification;
import com.toeicify.toeic.entity.UserFcmToken;
import com.toeicify.toeic.repository.NotificationRepository;
import com.toeicify.toeic.repository.UserFcmTokenRepository;
import com.toeicify.toeic.service.NotificationService;
import com.toeicify.toeic.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by hungpham on 8/18/2025
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserFcmTokenRepository userFcmTokenRepository;

    @Async
    @Override
    public void saveNotification(Long userId, String title, String content){
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        notificationRepository.save(n);
    }

    @Async
    @Override
    public void sendNotification(Long userId, String title, String content) {
        // Lưu thông báo vào db
        saveNotification(userId, title, content);

        // Lấy token của user
        List<UserFcmToken> tokens = userFcmTokenRepository.findByUserId(userId);

        // Gửi FCM
        for (UserFcmToken t : tokens) {
//            Message message = Message.builder()
//                    .setToken(t.getToken())
//                    .setNotification(
//                            com.google.firebase.messaging.Notification.builder()
//                                    .setTitle(title)
//                                    .setBody(content)
//                                    .build()
//                    )
//                    .build();
            Message message = Message.builder()
                    .setToken(t.getToken())
                    .putData("title", title)
                    .putData("body", content)
                    .build();
            try {
                FirebaseMessaging.getInstance().send(message);
            } catch (FirebaseMessagingException e) {
                e.printStackTrace();
            }
        }
    }

    @Transactional
    @Override
    public void registerToken(Long userId, String token) {
        boolean exists = userFcmTokenRepository.existsByUserIdAndToken(userId, token);
        if (!exists) {
            UserFcmToken userToken = UserFcmToken.builder()
                    .userId(userId)
                    .token(token)
                    .build();
            userFcmTokenRepository.save(userToken);
        }
    }

    @Override
    public PaginationResponse getNotificationsByUser(int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PaginationResponse.from(notifications, pageable);
    }

    @Async
    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId, LocalDateTime.now());
    }


    /**
     * Đánh dấu tất cả thông báo của user đã đọc
     */
    @Async
    @Transactional
    @Override
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    @Async
    @Transactional
    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        notificationRepository.findByIdAndUserId(notificationId, userId)
                .ifPresent(notificationRepository::delete);
    }

    @Async
    @Transactional
    @Override
    public void deleteAllNotifications(Long userId) {
        notificationRepository.deleteByUserId(userId);
    }
}
