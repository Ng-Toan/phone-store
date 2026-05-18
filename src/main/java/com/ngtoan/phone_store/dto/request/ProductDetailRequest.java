package com.ngtoan.phone_store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDetailRequest {

    @NotBlank(message = "RAM is required")
    @Size(max = 100)
    String ram;

    @NotBlank(message = "Storage is required")
    @Size(max = 100)
    String storage;

    @NotBlank(message = "CPU is required")
    @Size(max = 150)
    String cpu;

    @NotBlank(message = "Screen is required")
    @Size(max = 150)
    String screen;

    @NotBlank(message = "Battery is required")
    @Size(max = 100)
    String battery;

    @NotBlank(message = "Camera is required")
    @Size(max = 200)
    String camera;

    @NotBlank(message = "OS is required")
    @Size(max = 100)
    String os;

    @NotBlank(message = "Charging speed is required")
    @Size(max = 100)
    String chargingSpeed;

    @NotBlank(message = "Connectivity is required")
    @Size(max = 200)
    String connectivity;
}