package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "[Order]")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer orderID;

    Integer userID;

    String orderCode;

    LocalDateTime createdDate;

    BigDecimal totalAmount;

    @Enumerated(EnumType.ORDINAL)
    OrderStatus status;

    String customerName;

    String phone;

    String address;

    @Enumerated(EnumType.STRING)
    PaymentMethod paymentMethod;
}