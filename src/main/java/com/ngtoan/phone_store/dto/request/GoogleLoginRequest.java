package com.ngtoan.phone_store.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {

    @NotBlank(message = "Thiếu Google idToken")
    private String idToken;
}