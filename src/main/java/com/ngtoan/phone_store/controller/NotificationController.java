package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.dto.response.NotificationResponse;
import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.UserRepository;
import com.ngtoan.phone_store.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping("/my")
    public List<NotificationResponse> getMyNotifications(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return notificationService.getUserNotifications(user.getUserId());
    }

    @GetMapping("/my/unread-count")
    public Map<String, Long> countMyUnread(Authentication authentication) {
        User user = getCurrentUser(authentication);
        Long count = notificationService.countUserUnread(user.getUserId());
        return Map.of("count", count);
    }

    @GetMapping("/admin")
    public List<NotificationResponse> getAdminNotifications() {
        return notificationService.getAdminNotifications();
    }

    @GetMapping("/admin/unread-count")
    public Map<String, Long> countAdminUnread() {
        Long count = notificationService.countAdminUnread();
        return Map.of("count", count);
    }

    @PutMapping("/{notificationID}/read")
    public Map<String, String> markAsRead(@PathVariable Integer notificationID) {
        notificationService.markAsRead(notificationID);
        return Map.of("message", "Đã đánh dấu thông báo là đã đọc");
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResourceNotFoundException("User not found");
        }

        User user = userRepository.findByUsername(authentication.getName());

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        return user;
    }
}
