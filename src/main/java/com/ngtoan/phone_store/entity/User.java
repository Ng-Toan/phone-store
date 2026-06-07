package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "User")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    int userId;

    @Column(name = "Username", length = 50, nullable = false)
    String username;

    @Column(name = "Password", length = 255, nullable = false)
    String password;

    @Column(name = "FullName", length = 100, nullable = false)
    String fullName;

    @Column(name = "Email", length = 100, nullable = false)
    String email;

    @NotBlank
    @Column(name = "Phone", length = 20, nullable = false)
    String phone;

    @Column(name = "Gender", length = 20)
    String gender;

    @Column(name = "BirthDate")
    LocalDate birthDate;

    @Column(name = "Address", length = 255)
    String address;

    @Column(name = "RoleID", nullable = false)
    int roleId;

    @Column(name = "LevelID")
    Integer levelId;

    @Column(name = "TotalSpent")
    BigDecimal totalSpent;

    // true: tài khoản hoạt động / đã xác thực
    // false: chưa xác thực hoặc bị khóa
    @Column(name = "Status")
    Boolean status;

    // false: chưa xóa
    // true: đã xóa mềm
    @Column(name = "IsDeleted")
    Boolean deleted = false;

    @Column(name = "CreatedDate")
    LocalDateTime createdDate;
}