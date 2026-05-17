package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.response.NotificationResponse;
import com.ngtoan.phone_store.entity.Notification;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String ADMIN_ROLE_TARGET = "ADMIN";

    private final NotificationRepository notificationRepository;

    public void notifyUser(
            Integer userID,
            String title,
            String message,
            String type,
            Integer orderID
    ) {
        Notification notification = Notification.builder()
                .userID(userID)
                .roleTarget(null)
                .title(title)
                .message(message)
                .type(type)
                .relatedOrderID(orderID)
                .isRead(false)
                .createdDate(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    public void notifyAdmin(
            String title,
            String message,
            String type,
            Integer orderID
    ) {
        Notification notification = Notification.builder()
                .userID(null)
                .roleTarget(ADMIN_ROLE_TARGET)
                .title(title)
                .message(message)
                .type(type)
                .relatedOrderID(orderID)
                .isRead(false)
                .createdDate(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    public List<NotificationResponse> getUserNotifications(Integer userID) {
        return notificationRepository.findByUserIDOrderByCreatedDateDesc(userID)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<NotificationResponse> getAdminNotifications() {
        return notificationRepository.findByRoleTargetOrderByCreatedDateDesc(ADMIN_ROLE_TARGET)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Long countUserUnread(Integer userID) {
        return notificationRepository.countByUserIDAndIsReadFalse(userID);
    }

    public Long countAdminUnread() {
        return notificationRepository.countByRoleTargetAndIsReadFalse(ADMIN_ROLE_TARGET);
    }

    public void markAsRead(Integer notificationID) {
        Notification notification = notificationRepository.findById(notificationID)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();

        response.setNotificationID(notification.getNotificationID());
        response.setUserID(notification.getUserID());
        response.setRoleTarget(notification.getRoleTarget());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setRelatedOrderID(notification.getRelatedOrderID());
        response.setIsRead(notification.getIsRead());
        response.setCreatedDate(notification.getCreatedDate());

        return response;
    }
}
