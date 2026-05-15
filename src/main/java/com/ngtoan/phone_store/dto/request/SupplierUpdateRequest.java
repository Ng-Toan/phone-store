package com.ngtoan.phone_store.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupplierUpdateRequest {

    @Size(max = 100)
    String name;

    @Size(max = 20)
    String phone;

    @Size(max = 100)
    String email;

    @Size(max = 255)
    String address;

    String status;
}
