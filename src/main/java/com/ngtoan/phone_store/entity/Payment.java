package com.ngtoan.phone_store.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;


@Entity
@Table(name = "Payment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer paymentID;

    Integer orderID;

    @Enumerated(EnumType.STRING)
    PaymentMethod method;

    BigDecimal amount;

    LocalDateTime paymentDate;

    @Enumerated(EnumType.ORDINAL)
    PaymentStatus status;

    @Column(name = "transactionCode")
    String transactionCode;

    @Column(nullable = false)
    LocalDateTime createdDate;

}
