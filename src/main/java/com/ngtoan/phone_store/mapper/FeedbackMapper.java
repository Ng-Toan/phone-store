package com.ngtoan.phone_store.mapper;

import com.ngtoan.phone_store.dto.response.FeedbackResponse;
import com.ngtoan.phone_store.entity.Feedback;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    FeedbackResponse toResponse(Feedback feedback);
}