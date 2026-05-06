package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.Order;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByUserID(Integer userID);

    @Query(
            value = """
                    SELECT ISNULL(SUM(TotalAmount), 0)
                    FROM [Order]
                    WHERE UserID = :userID
                      AND Status = 3
                    """,
            nativeQuery = true
    )
    BigDecimal calculateTotalSpentByUserID(@Param("userID") Integer userID);
}