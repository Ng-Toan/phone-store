package com.ngtoan.phone_store.mapper;

import com.ngtoan.phone_store.dto.response.MembershipLevelResponse;
import com.ngtoan.phone_store.entity.MembershipLevel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MembershipLevelMapper {

    MembershipLevelResponse toResponse(MembershipLevel membershipLevel);
}