package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageResponse {

    Long messageId;
    Integer roomId;
    Integer senderId;
    String senderName;
    String message;
    Boolean isRead;
    LocalDateTime createdDate;
}