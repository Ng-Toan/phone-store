package com.ngtoan.phone_store.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDetailResponse {
    private Integer orderDetailID;
    private Integer productID;
    private String productName;
    private String image;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}