package com.ngtoan.phone_store.dto.response;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Integer orderID;
    BigDecimal totalAmount;
    String status;
}
