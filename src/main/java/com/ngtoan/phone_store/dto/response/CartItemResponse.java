package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {

    Integer cartItemID;
    Integer productID;
    Integer quantity;

}