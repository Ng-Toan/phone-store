package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "EmailVerificationToken")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TokenID")
    Integer tokenID;

    @Column(name = "UserID", nullable = false)
    Integer userID;

    @Column(name = "Email", nullable = false, length = 100)
    String email;

    @Column(name = "OtpCode", nullable = false, length = 10)
    String otpCode;

    @Column(name = "ExpiredAt", nullable = false)
    LocalDateTime expiredAt;

    @Column(name = "Used", nullable = false)
    Boolean used;

    @Column(name = "CreatedAt", nullable = false)
    LocalDateTime createdAt;
}