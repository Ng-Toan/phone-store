package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartResponse {

    Integer cartID;
    List<CartItemResponse> items;

    Integer totalQuantity;
    Double totalPrice;
}