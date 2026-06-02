package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRoomResponse {

    Integer roomId;
    Integer userId;
    String username;
    String fullName;
    String email;
    String lastMessage;
    LocalDateTime updatedDate;
    Long unreadCount;
}