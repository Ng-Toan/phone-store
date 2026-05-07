package com.ngtoan.phone_store.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderAdminResponse {
    private Integer orderID;
    private Integer userID;
    private String orderCode;
    private LocalDateTime createdDate;
    private BigDecimal totalAmount;
    private String status;

    private String customerName;
    private String phone;
    private String address;
    private String paymentMethod;

    private List<OrderDetailResponse> items;

     private PaymentResponse payment;
}