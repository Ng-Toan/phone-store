package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "OrderDetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer orderDetailID;

    Integer orderID;

    Integer productID;

    String productName;

    String image;

    Integer quantity;

    BigDecimal price;

    BigDecimal subtotal;
}