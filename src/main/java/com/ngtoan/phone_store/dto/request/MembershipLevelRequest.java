package com.ngtoan.phone_store.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MembershipLevelRequest {

    @NotBlank(message = "Level name is required")
    String levelName;

    @NotNull(message = "Discount percent is required")
    @DecimalMin(value = "0.00", message = "Discount percent must be greater than or equal to 0")
    @DecimalMax(value = "100.00", message = "Discount percent must be less than or equal to 100")
    BigDecimal discountPercent;

    @NotNull(message = "Min spent is required")
    @DecimalMin(value = "0.00", message = "Min spent must be greater than or equal to 0")
    BigDecimal minSpent;
}