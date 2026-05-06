package com.ngtoan.phone_store.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserMembershipResponse {

    Integer userID;

    String username;

    String fullName;

    BigDecimal totalSpent;

    Integer levelID;

    String levelName;

    BigDecimal discountPercent;

    BigDecimal minSpent;
}