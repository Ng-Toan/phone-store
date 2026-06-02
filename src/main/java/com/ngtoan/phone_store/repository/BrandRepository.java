package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
}