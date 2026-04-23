package com.ngtoan.phone_store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ngtoan.phone_store.entity.ProductDetail;

public interface ProductDetailRepository
        extends JpaRepository<ProductDetail,Integer> {

    Optional<ProductDetail> findByProductID(Integer productID);
}
