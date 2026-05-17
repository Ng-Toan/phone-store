package com.ngtoan.phone_store.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ngtoan.phone_store.dto.response.AdminPaymentResponse;
import com.ngtoan.phone_store.entity.Order;
import com.ngtoan.phone_store.entity.OrderStatus;
import com.ngtoan.phone_store.entity.Payment;
import com.ngtoan.phone_store.entity.PaymentMethod;
import com.ngtoan.phone_store.entity.PaymentStatus;
import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.OrderRepository;
import com.ngtoan.phone_store.repository.PaymentRepository;
import com.ngtoan.phone_store.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

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
                .note(method == PaymentMethod.COD
                        ? "Thanh toán khi nhận hàng"
                        : "Chờ thanh toán online")
                .build();

        // save lần 1 để có paymentID
        paymentRepository.save(payment);

        // generate paymentCode
        payment.setPaymentCode(
                "TT" + String.format("%05d", payment.getPaymentID())
        );

        // save lần 2
        return paymentRepository.save(payment);
    }

    // 2. Thanh toán thành công ONLINE
    public void handlePaymentSuccess(Integer paymentID) {

        Payment payment = paymentRepository.findById(paymentID)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found")
                );

        // Tránh xác nhận thanh toán nhiều lần
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        Order order = orderRepository.findById(payment.getOrderID())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found")
                );

        // Nếu đơn đã bị hủy thì không cho thanh toán thành công nữa
        if (order.getStatus() == OrderStatus.CANCELLED) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setNote("Thanh toán thất bại vì đơn hàng đã bị hủy");
            paymentRepository.save(payment);
            return;
        }

        /*
         * QUAN TRỌNG:
         * Thanh toán thành công chỉ cập nhật Payment.
         * Không tự đổi Order sang CONFIRMED.
         * Không trừ kho.
         * Không cộng TotalSpent.
         * Không cập nhật hạng thành viên.
         *
         * Admin xác nhận đơn trong OrderService thì mới đổi Order sang CONFIRMED.
         * Admin hoàn thành đơn DELIVERED thì mới cộng TotalSpent và xét hạng.
         */

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTransactionCode("TXN" + System.currentTimeMillis());
        payment.setNote("Thanh toán thành công, chờ admin xác nhận đơn hàng");

        paymentRepository.save(payment);
    }

    // 3. Thanh toán thất bại ONLINE
    public void handlePaymentFail(Integer paymentID) {

        Payment payment = paymentRepository.findById(paymentID)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found")
                );

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setNote("Thanh toán thất bại");

        Order order = orderRepository.findById(payment.getOrderID())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found")
                );

        order.setStatus(OrderStatus.CANCELLED);

        paymentRepository.save(payment);
        orderRepository.save(order);
    }

    // 4. COD: sync payment theo order
    public void syncPaymentWithOrder(Order order) {

        Payment payment = paymentRepository.findByOrderID(order.getOrderID())
                .orElse(null);

        if (payment == null) {
            return;
        }

        if (payment.getMethod() != PaymentMethod.COD) {
            return;
        }

        switch (order.getStatus()) {
            case DELIVERED:
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setNote("COD đã thanh toán");
                break;

            case CANCELLED:
                payment.setStatus(PaymentStatus.FAILED);
                payment.setNote("Đơn hàng đã bị hủy");
                break;

            default:
                return;
        }

        paymentRepository.save(payment);
    }

    // 5. Lấy tất cả payment
    public List<AdminPaymentResponse> getAllPayments() {

        List<Payment> payments = paymentRepository.findAll()
                .stream()
                .sorted((a, b) ->
                        b.getCreatedDate().compareTo(a.getCreatedDate())
                )
                .toList();

        return payments.stream().map(payment -> {

            Order order = orderRepository.findById(payment.getOrderID())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Order not found")
                    );

            User user = userRepository.findById(order.getUserID())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("User not found")
                    );

            AdminPaymentResponse response = new AdminPaymentResponse();

            response.setPaymentID(payment.getPaymentID());
            response.setPaymentCode(payment.getPaymentCode());

            response.setOrderID(order.getOrderID());
            response.setOrderCode(order.getOrderCode());

            response.setCustomerName(user.getFullName());

            response.setMethod(payment.getMethod().name());
            response.setStatus(payment.getStatus().name());
            response.setOrderStatus(order.getStatus().name());

            response.setAmount(payment.getAmount());
            response.setPaymentDate(payment.getPaymentDate());
            response.setTransactionCode(payment.getTransactionCode());
            response.setNote(payment.getNote());

            return response;

        }).toList();
    }

    // 6. Lấy chi tiết 1 payment
    public AdminPaymentResponse getPaymentById(Integer id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found")
                );

        Order order = orderRepository.findById(payment.getOrderID())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found")
                );

        User user = userRepository.findById(order.getUserID())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        AdminPaymentResponse response = new AdminPaymentResponse();

        response.setPaymentID(payment.getPaymentID());
        response.setPaymentCode(payment.getPaymentCode());

        response.setOrderID(order.getOrderID());
        response.setOrderCode(order.getOrderCode());

        response.setCustomerName(user.getFullName());

        response.setMethod(payment.getMethod().name());
        response.setStatus(payment.getStatus().name());
        response.setOrderStatus(order.getStatus().name());

        response.setAmount(payment.getAmount());
        response.setPaymentDate(payment.getPaymentDate());
        response.setTransactionCode(payment.getTransactionCode());
        response.setNote(payment.getNote());

        return response;
    }
}