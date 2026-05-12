package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminPaymentResponse {

    Integer paymentID;

    String paymentCode;

    Integer orderID;

    String orderCode;

    String customerName;

    String method;

    String status;

    String orderStatus;

    BigDecimal amount;

    LocalDateTime paymentDate;

    String transactionCode;

    String note;
}