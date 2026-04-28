package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingSummaryResponse {

    Double averageRating;

    Integer totalReviews;

    Map<Integer, Long> ratingBreakdown;
}