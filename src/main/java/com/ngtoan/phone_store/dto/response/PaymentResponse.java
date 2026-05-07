package com.ngtoan.phone_store.dto.response;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class PaymentResponse {
    Integer paymentID;
    String method;
    String status;
    BigDecimal amount;
    LocalDateTime paymentDate;
    String transactionCode;
}
