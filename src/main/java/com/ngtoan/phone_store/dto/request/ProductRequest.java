package com.ngtoan.phone_store.dto.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {

    @NotBlank
    String name;

    String image;

    @NotNull
    BigDecimal price;

    BigDecimal promotionPrice;

    BigDecimal vat;

    Integer quantity;

    Integer warranty;

    Boolean isHot;

    Integer status;

    String description;

    @NotNull
    Integer categoryID;

    Integer brandID;

    Integer supplierID;
}