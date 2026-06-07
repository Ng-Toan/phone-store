package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatbotProductDTO {

    Long productID;
    String productName;
    String brandName;
    String categoryName;
    String image;

    BigDecimal price;
    BigDecimal promotionPrice;
    BigDecimal finalPrice;
    BigDecimal vat;

    Integer quantity;
    Boolean isHot;
    String description;

    String ram;
    String storage;
    String cpu;
    String screen;
    String battery;
    String camera;
    String os;
    String chargingSpeed;
    String connectivity;
}