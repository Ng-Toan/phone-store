package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {

    Integer orderID;
    String orderCode;
    BigDecimal totalAmount;
    String status;
    LocalDateTime createdDate;
    PaymentResponse payment;
}