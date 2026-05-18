package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "MembershipLevel")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LevelID")
    Integer levelID;

    @Column(name = "LevelName", nullable = false)
    String levelName;

    @Column(name = "DiscountPercent", nullable = false)
    BigDecimal discountPercent;

    @Column(name = "MinSpent", nullable = false)
    BigDecimal minSpent;

    // true: hạng mặc định của hệ thống, không cho xóa
    // false: hạng admin tự thêm, có thể xóa mềm
    @Builder.Default
    @Column(name = "IsDefault", nullable = false)
    Boolean isDefault = false;

    // true: đã xóa mềm
    // false: đang sử dụng
    @Builder.Default
    @Column(name = "IsDeleted", nullable = false)
    Boolean isDeleted = false;
}