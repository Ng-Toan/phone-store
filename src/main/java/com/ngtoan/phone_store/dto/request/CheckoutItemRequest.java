package com.ngtoan.phone_store.dto.request;

import lombok.Data;

@Data
public class CheckoutItemRequest {

    Integer productID;
    Integer quantity;
}