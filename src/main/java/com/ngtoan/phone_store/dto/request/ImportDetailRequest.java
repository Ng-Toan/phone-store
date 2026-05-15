package com.ngtoan.phone_store.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImportDetailRequest {

    @NotNull(message = "Product is required")
    Integer productID;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    Integer quantity;

    @NotNull(message = "Import price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Import price must be greater than 0")
    BigDecimal importPrice;
}
