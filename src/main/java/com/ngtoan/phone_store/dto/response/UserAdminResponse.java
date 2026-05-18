package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAdminResponse {

    Integer userId;
    String username;
    String fullName;
    String email;
    String phone;

    Integer roleId;
    String roleName;

    Integer levelId;
    String levelName;
    BigDecimal discountPercent;
    BigDecimal minSpent;

    BigDecimal totalSpent;

    Boolean status;
    String statusName;

    Boolean deleted;

    LocalDateTime createdDate;
}