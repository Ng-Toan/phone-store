package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipLevelResponse {

    Integer levelID;

    String levelName;

    BigDecimal discountPercent;

    BigDecimal minSpent;

    Boolean isDefault;

    Boolean isDeleted;
}