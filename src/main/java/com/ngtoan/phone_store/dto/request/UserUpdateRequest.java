package com.ngtoan.phone_store.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    String fullName;
    @Email(message = "Email is invalid")
    String email;
    String phone;

    // dành cho admin
    Integer roleId;
    Boolean status;
}
