package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderAdminResponse {
    Integer orderID;
    Integer userID;
    String orderCode;
    LocalDateTime createdDate;
    BigDecimal totalAmount;
    String status;

    String customerName;
    String phone;
    String address;
    String paymentMethod;
    String note;

    List<OrderDetailResponse> items;

     PaymentResponse payment;
}