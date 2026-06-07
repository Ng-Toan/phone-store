package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "PasswordResetOtp")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordResetOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ResetID")
    Integer resetID;

    @Column(name = "Email", nullable = false, length = 100)
    String email;

    @Column(name = "OtpCode", nullable = false, length = 10)
    String otpCode;

    @Column(name = "ExpiredAt", nullable = false)
    LocalDateTime expiredAt;

    @Column(name = "Used", nullable = false)
    Boolean used = false;

    @Column(name = "CreatedAt", nullable = false)
    LocalDateTime createdAt;
}