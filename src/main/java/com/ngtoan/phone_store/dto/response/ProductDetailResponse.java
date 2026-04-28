package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ProductDetailResponse {
    String ram;
    String storage;
    String cpu;
    String screen;
    String battery;
    String camera;
    String os;
    String chargingSpeed;
    String connectivity;
}
