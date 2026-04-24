package com.ngtoan.phone_store.dto.response;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {

    Integer cartItemID;
    Integer productID;
    String productName;
    String image;

    BigDecimal price;
    Integer quantity;

    BigDecimal subtotal;
}