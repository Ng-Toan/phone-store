package com.ngtoan.phone_store.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.ngtoan.phone_store.dto.request.ProductRequest;
import com.ngtoan.phone_store.dto.request.ProductUpdateRequest;
import com.ngtoan.phone_store.dto.response.ProductResponse;
import com.ngtoan.phone_store.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(ProductRequest request);

    ProductResponse toResponse(Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProduct(@MappingTarget Product product, ProductUpdateRequest request);
}
