package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailResponse {
    Integer orderDetailID;
    Integer productID;
    String productName;
    String image;
    Integer quantity;
    BigDecimal price;
    BigDecimal subtotal;
}