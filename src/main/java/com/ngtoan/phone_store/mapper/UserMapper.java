package com.ngtoan.phone_store.mapper;

import com.ngtoan.phone_store.dto.request.UserCreationRequest;
import com.ngtoan.phone_store.dto.request.UserUpdateRequest;
import com.ngtoan.phone_store.entity.User;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdDate", expression = "java(java.time.LocalDateTime.now())")
    User toEntity(UserCreationRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
     @Mapping(target = "roleId", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest dto);
}