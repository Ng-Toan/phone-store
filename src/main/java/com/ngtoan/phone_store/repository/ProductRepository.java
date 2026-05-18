package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.Product;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // 🔹 Check trùng tên (phân biệt hoa thường)
    boolean existsByName(String name);

    // 🔹 Check trùng tên (không phân biệt hoa thường - nên dùng)
    boolean existsByNameIgnoreCase(String name);

    // 🔹 Search theo tên (LIKE %name%)
    List<Product> findByNameContainingIgnoreCase(String name);

    // 🔥 THÊM CÁI NÀY (QUAN TRỌNG)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.productID = :id")
    Product findByIdForUpdate(@Param("id") Integer id);

    long countByStatus(Integer status);

    long countByQuantityLessThanEqualAndStatus(Integer quantity, Integer status);

    @Query("""
                SELECT p.productID, p.name, p.quantity
                FROM Product p
                WHERE p.status = 1
                  AND p.quantity <= :limit
                ORDER BY p.quantity ASC
            """)
    List<Object[]> findLowStockProducts(@Param("limit") Integer limit);

    List<Product> findByStatusNot(Integer status);

}