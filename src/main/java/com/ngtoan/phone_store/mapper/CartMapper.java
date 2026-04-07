package com.ngtoan.phone_store.mapper;

import com.ngtoan.phone_store.dto.response.CartItemResponse;
import com.ngtoan.phone_store.entity.CartItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartMapper {

    CartItemResponse toResponse(CartItem cartItem);

}