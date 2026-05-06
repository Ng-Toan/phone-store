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
}