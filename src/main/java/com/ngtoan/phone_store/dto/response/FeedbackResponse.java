package com.ngtoan.phone_store.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackResponse {

    Integer feedbackID;
    Integer userID;
    String userFullName;
    Integer productID;
    String productName;
    String comment;
    Integer rating;
    LocalDateTime createdDate;
}