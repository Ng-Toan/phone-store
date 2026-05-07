package com.ngtoan.phone_store.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.ngtoan.phone_store.entity.Order;
import com.ngtoan.phone_store.entity.OrderStatus;
import com.ngtoan.phone_store.entity.Payment;
import com.ngtoan.phone_store.entity.PaymentMethod;
import com.ngtoan.phone_store.entity.PaymentStatus;
import com.ngtoan.phone_store.repository.OrderRepository;
import com.ngtoan.phone_store.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    // 1. Tạo payment
    public Payment createPayment(Integer orderID, PaymentMethod method) {

        Order order = orderRepository.findById(orderID)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = Payment.builder()
                .orderID(orderID)
                .method(method)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .createdDate(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    // 2. Thanh toán thành công (ONLINE)
    public void handlePaymentSuccess(Integer paymentID) {

        Payment payment = paymentRepository.findById(paymentID)
                .orElseThrow();

        if(payment.getStatus() != PaymentStatus.PENDING) return;

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTransactionCode("TXN" + System.currentTimeMillis());

        Order order = orderRepository.findById(payment.getOrderID())
                .orElseThrow();

        order.setStatus(OrderStatus.CONFIRMED);

        paymentRepository.save(payment);
        orderRepository.save(order);
    }

    // 3. Thanh toán thất bại (ONLINE)
    public void handlePaymentFail(Integer paymentID) {

        Payment payment = paymentRepository.findById(paymentID)
                .orElseThrow();

        if(payment.getStatus() != PaymentStatus.PENDING) return;

        payment.setStatus(PaymentStatus.FAILED);

        Order order = orderRepository.findById(payment.getOrderID())
                .orElseThrow();

        order.setStatus(OrderStatus.CANCELLED);

        paymentRepository.save(payment);
        orderRepository.save(order);
    }

    // 4. COD: sync payment theo order
    public void syncPaymentWithOrder(Order order) {

        Payment payment = paymentRepository.findByOrderID(order.getOrderID())
                .orElse(null);

        if(payment == null) return;

        if(payment.getMethod() != PaymentMethod.COD) return;

        switch (order.getStatus()) {
            case DELIVERED:
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPaymentDate(LocalDateTime.now());
                break;

            case CANCELLED:
                payment.setStatus(PaymentStatus.FAILED);
                break;

            default:
                return;
        }

        paymentRepository.save(payment);
    }
}
