package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // 🔹 Check trùng tên (phân biệt hoa thường)
    boolean existsByName(String name);

    // 🔹 Check trùng tên (không phân biệt hoa thường - nên dùng)
    boolean existsByNameIgnoreCase(String name);

    // 🔹 Search theo tên (LIKE %name%)
    List<Product> findByNameContainingIgnoreCase(String name);

}