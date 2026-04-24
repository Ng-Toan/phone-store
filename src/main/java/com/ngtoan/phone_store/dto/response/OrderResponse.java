package com.ngtoan.phone_store.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponse {

    Integer orderID;
    String orderCode;
    BigDecimal totalAmount;
    String status;
    LocalDateTime createdDate;
}