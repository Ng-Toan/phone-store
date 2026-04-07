package com.ngtoan.phone_store.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductUpdateRequest {

    @Size(max = 150)
    String name;

    String image;

    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal price;

    BigDecimal promotionPrice;

    @DecimalMin(value = "0.0")
    BigDecimal vat;

    @Min(0)
    Integer quantity;

    @Min(0)
    Integer warranty;

    Boolean isHot;

    String description;

    String detail;

    Integer categoryID;
    Integer brandID;
    Integer supplierID;
}
