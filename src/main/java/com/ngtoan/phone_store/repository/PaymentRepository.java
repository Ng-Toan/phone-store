package com.ngtoan.phone_store.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ngtoan.phone_store.entity.Payment;
import com.ngtoan.phone_store.entity.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByOrderID(Integer orderID);

    @Query("""
        SELECT p FROM Payment p
        WHERE p.status = :status
        AND p.createdDate <= :expiredTime
    """)
    List<Payment> findExpiredPendingPayments(
        @Param("status") PaymentStatus status,
        @Param("expiredTime") LocalDateTime expiredTime
    );
}