package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.ImportReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;

public interface ImportReceiptRepository extends JpaRepository<ImportReceipt, Integer> {
    List<ImportReceipt> findAllByOrderByCreatedDateDesc();
    List<ImportReceipt> findBySupplierIDOrderByCreatedDateDesc(Integer supplierID);

    long countByCreatedDateBetween(LocalDateTime start, LocalDateTime end);

@Query("""
    SELECT COALESCE(SUM(i.totalAmount), 0)
    FROM ImportReceipt i
    WHERE i.status = 'COMPLETED'
      AND i.createdDate >= :start
      AND i.createdDate < :end
""")
BigDecimal sumCompletedImportAmountByDateRange(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
);
}
