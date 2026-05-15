package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImportDetailResponse {
    Integer importDetailID;
    Integer productID;
    String productName;
    String image;
    Integer quantity;
    BigDecimal importPrice;
    BigDecimal subTotal;
}
