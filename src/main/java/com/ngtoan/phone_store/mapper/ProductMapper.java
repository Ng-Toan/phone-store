package com.ngtoan.phone_store.mapper;

import org.mapstruct.*;

import com.ngtoan.phone_store.dto.request.ProductRequest;
import com.ngtoan.phone_store.dto.request.ProductUpdateRequest;
import com.ngtoan.phone_store.dto.response.ProductResponse;
import com.ngtoan.phone_store.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "detail", ignore = true)
    Product toEntity(ProductRequest request);

    @Mappings({
            @Mapping(target = "categoryName", source = "category.name"),
            @Mapping(target = "brandName", source = "brand.name"),
            @Mapping(target = "supplierName", source = "supplier.name"),
            @Mapping(target = "status", source = "status"),
            @Mapping(target = "detail", ignore = true)
    })
    ProductResponse toResponse(Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "detail", ignore = true)
    void updateProduct(@MappingTarget Product product, ProductUpdateRequest request);
}