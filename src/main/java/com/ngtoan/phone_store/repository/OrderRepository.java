package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.Order;
import com.ngtoan.phone_store.entity.OrderStatus;
import java.time.LocalDateTime;

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
                      AND Status IN (1, 2, 3)
                    """,
            nativeQuery = true
    )
    BigDecimal calculateTotalSpentByUserID(
            @Param("userID") Integer userID
    );

    long countByCreatedDateBetween(LocalDateTime start, LocalDateTime end);

long countByStatus(OrderStatus status);

long countByStatusAndCreatedDateBetween(
        OrderStatus status,
        LocalDateTime start,
        LocalDateTime end
);

List<Order> findTop4ByOrderByCreatedDateDesc();

@Query("""
    SELECT COALESCE(SUM(o.totalAmount), 0)
    FROM Order o
    WHERE o.status = :status
      AND o.createdDate >= :start
      AND o.createdDate < :end
""")
BigDecimal sumRevenueByStatusAndDateRange(
        @Param("status") OrderStatus status,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
);
}