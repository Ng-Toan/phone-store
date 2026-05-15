package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImportReceiptResponse {
    Integer importID;
    Integer supplierID;
    String supplierName;
    LocalDateTime createdDate;
    BigDecimal totalAmount;
    String note;
    String status;
    List<ImportDetailResponse> details;
}
