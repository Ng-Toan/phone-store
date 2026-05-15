package com.ngtoan.phone_store.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    Integer userId;
    String username;
    String fullName;
    String email;
    String phone;
    String gender;

    // Field cũ theo backend/database
    LocalDate birthDate;
    String address;

    // Field mới theo frontend profile
    LocalDate birthday;
    String defaultAddress;

    Integer roleId;
    Integer levelId;
    BigDecimal totalSpent;
    Boolean status;
    LocalDateTime createdDate;
}
