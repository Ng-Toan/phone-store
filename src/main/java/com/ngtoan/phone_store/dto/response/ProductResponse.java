package com.ngtoan.phone_store.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {

     Integer productID;
     String name;
     String image;
     BigDecimal price;
     Integer quantity;
     Boolean isHot;
     String description;
     Integer categoryID;
    LocalDateTime createdDate;
    LocalDateTime updatedDate;
}
