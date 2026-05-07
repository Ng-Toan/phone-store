package com.ngtoan.phone_store.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendVerificationRequest {

    @Email(message = "Email is invalid")
    @NotBlank(message = "Email is required")
    String email;
}