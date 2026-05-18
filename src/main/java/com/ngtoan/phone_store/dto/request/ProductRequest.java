package com.ngtoan.phone_store.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    String name;

    String image;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price;

    BigDecimal promotionPrice;

    @DecimalMin(value = "0.0", message = "VAT must be >= 0")
    BigDecimal vat;

    @Min(value = 0, message = "Quantity must be >= 0")
    Integer quantity;

    @Min(value = 0, message = "Warranty must be >= 0")
    Integer warranty;

    Boolean isHot;

    Integer status;

    String description;

    @NotNull(message = "Category is required")
    Integer categoryID;

    Integer brandID;

    Integer supplierID;

    @Valid
    @NotNull(message = "Product detail is required")
    ProductDetailRequest detail;
}