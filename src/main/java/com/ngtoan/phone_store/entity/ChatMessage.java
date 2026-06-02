package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatmessage")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MessageID")
    Long messageId;

    @Column(name = "RoomID", nullable = false)
    Integer roomId;

    @Column(name = "SenderID", nullable = false)
    Integer senderId;

    @Column(name = "Message", nullable = false)
    String message;

    @Column(name = "IsRead")
    Boolean isRead;

    @Column(name = "CreatedDate")
    LocalDateTime createdDate;
}