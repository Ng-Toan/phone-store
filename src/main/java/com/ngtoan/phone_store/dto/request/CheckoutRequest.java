package com.ngtoan.phone_store.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequest {

    String customerName;
    String phone;
    String address;
    String paymentMethod;

    List<CheckoutItemRequest> items;
}