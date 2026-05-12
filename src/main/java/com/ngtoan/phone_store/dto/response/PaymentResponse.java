package com.ngtoan.phone_store.dto.response;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
    Integer paymentID;
    String method;
    String status;
    BigDecimal amount;
    LocalDateTime paymentDate;
    String transactionCode;

    String paymentCode;
    String orderCode;
}
