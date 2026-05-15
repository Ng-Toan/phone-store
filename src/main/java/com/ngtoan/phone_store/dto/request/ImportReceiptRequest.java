package com.ngtoan.phone_store.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImportReceiptRequest {

    @NotNull(message = "Supplier is required")
    Integer supplierID;

    @Size(max = 255)
    String note;

    @NotEmpty(message = "Import details must not be empty")
    List<@Valid ImportDetailRequest> details;
}
