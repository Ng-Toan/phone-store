package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)public class NotificationResponse {
    Integer notificationID;
    Integer userID;
    String roleTarget;
    String title;
    String message;
    String type;
    Integer relatedOrderID;
    Boolean isRead;
    LocalDateTime createdDate;
}
