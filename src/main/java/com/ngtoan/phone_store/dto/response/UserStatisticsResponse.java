package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserStatisticsResponse {

    long totalUsers;
    long totalAdmins;
    long totalVipUsers;
    long totalLockedUsers;
}