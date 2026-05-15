package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupplierResponse {
    Integer supplierID;
    String name;
    String phone;
    String email;
    String address;
    String status;
    LocalDateTime createdDate;
    LocalDateTime updatedDate;
}
