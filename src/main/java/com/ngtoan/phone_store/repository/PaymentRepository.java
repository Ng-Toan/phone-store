package com.ngtoan.phone_store.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ngtoan.phone_store.dto.response.AdminPaymentResponse;
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

    @Query("""
        SELECT new com.ngtoan.phone_store.dto.response.AdminPaymentResponse(
            p.paymentID,
            p.paymentCode,
            o.orderID,
            o.orderCode,
            o.customerName,
            CAST(p.method as string),
            CAST(p.status as string),
            CAST(o.status as string),
            p.amount,
            p.paymentDate,
            p.transactionCode,
            p.note
        )
        FROM Payment p
        JOIN Order o ON p.orderID = o.orderID
        ORDER BY p.createdDate DESC
    """)
    List<AdminPaymentResponse> getAllAdminPayments();
}