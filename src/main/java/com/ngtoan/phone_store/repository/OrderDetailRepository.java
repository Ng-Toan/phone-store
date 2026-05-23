package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    List<OrderDetail> findByOrderID(Integer orderID);

    @Query("""
                SELECT
                    od.productID,
                    od.productName,
                    SUM(od.quantity),
                    COALESCE(SUM(od.subtotal), 0)
                FROM OrderDetail od
                JOIN com.ngtoan.phone_store.entity.Order o
                  ON od.orderID = o.orderID
                WHERE o.status = com.ngtoan.phone_store.entity.OrderStatus.DELIVERED
                  AND o.createdDate >= :start
                  AND o.createdDate < :end
                GROUP BY od.productID, od.productName
                ORDER BY SUM(od.quantity) DESC
            """)
    List<Object[]> getTopSellingProducts(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}