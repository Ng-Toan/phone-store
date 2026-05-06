package com.ngtoan.phone_store.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MembershipLevelResponse {

    Integer levelID;

    String levelName;

    BigDecimal discountPercent;

    BigDecimal minSpent;
}