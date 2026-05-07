package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "PendingUserRegistration")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PendingUserRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PendingID")
    Integer pendingID;

    @Column(name = "Username", nullable = false, length = 50)
    String username;

    @Column(name = "Password", nullable = false, length = 255)
    String password;

    @Column(name = "Email", nullable = false, length = 100)
    String email;

    @Column(name = "FullName", nullable = false, length = 100)
    String fullName;

    @Column(name = "Phone", nullable = false, length = 20)
    String phone;

    @Column(name = "RoleID", nullable = false)
    Integer roleId;

    @Column(name = "OtpCode", nullable = false, length = 10)
    String otpCode;

    @Column(name = "ExpiredAt", nullable = false)
    LocalDateTime expiredAt;

    @Column(name = "CreatedAt", nullable = false)
    LocalDateTime createdAt;
}