package com.ngtoan.phone_store.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ngtoan.phone_store.dto.response.OrderResponse;
import com.ngtoan.phone_store.entity.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    OrderResponse toDTO(Order order);
}
