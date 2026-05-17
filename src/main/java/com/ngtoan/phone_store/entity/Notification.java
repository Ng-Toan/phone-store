package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "Notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer notificationID;

    Integer userID;

    @Column(columnDefinition = "NVARCHAR(20)")
    String roleTarget;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    String title;

    @Column(nullable = false, columnDefinition = "NVARCHAR(500)")
    String message;

    @Column(nullable = false, columnDefinition = "NVARCHAR(50)")
    String type;

    Integer relatedOrderID;

    @Column(nullable = false)
    Boolean isRead;

    @Column(nullable = false)
    LocalDateTime createdDate;
}
