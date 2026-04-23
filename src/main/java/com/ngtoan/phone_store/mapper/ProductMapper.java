package com.ngtoan.phone_store.mapper;

import org.mapstruct.*;

import com.ngtoan.phone_store.dto.request.ProductRequest;
import com.ngtoan.phone_store.dto.request.ProductUpdateRequest;
import com.ngtoan.phone_store.dto.response.ProductResponse;
import com.ngtoan.phone_store.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(ProductRequest request);
    
    @Mappings({
        @Mapping(target = "categoryName", source = "category.name"),
        @Mapping(target = "brandName", source = "brand.name"),
        @Mapping(target = "supplierName", source = "supplier.name"),
        @Mapping(target = "status", source = "status")
    })
    ProductResponse toResponse(Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProduct(@MappingTarget Product product, ProductUpdateRequest request);
}
