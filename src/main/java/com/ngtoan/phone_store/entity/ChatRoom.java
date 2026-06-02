package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatroom")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoomID")
    Integer roomId;

    @Column(name = "UserID", nullable = false)
    Integer userId;

    @Column(name = "CreatedDate")
    LocalDateTime createdDate;

    @Column(name = "UpdatedDate")
    LocalDateTime updatedDate;
}