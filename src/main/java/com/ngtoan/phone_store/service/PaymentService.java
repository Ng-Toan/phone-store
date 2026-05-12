package com.ngtoan.phone_store.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ngtoan.phone_store.entity.Order;
import com.ngtoan.phone_store.entity.OrderDetail;
import com.ngtoan.phone_store.entity.OrderStatus;
import com.ngtoan.phone_store.entity.Payment;
import com.ngtoan.phone_store.entity.PaymentMethod;
import com.ngtoan.phone_store.entity.PaymentStatus;
import com.ngtoan.phone_store.entity.Product;
import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.exception.OutOfStockException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.OrderDetailRepository;
import com.ngtoan.phone_store.repository.OrderRepository;
import com.ngtoan.phone_store.repository.PaymentRepository;
import com.ngtoan.phone_store.repository.ProductRepository;
import com.ngtoan.phone_store.repository.UserRepository;
import com.ngtoan.phone_store.dto.response.AdminPaymentResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    private final MembershipLevelService membershipLevelService;
    private final UserRepository userRepository;
    
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;

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
                "TT" + String.format("%05d",
                        payment.getPaymentID()));

        // save lần 2
        return paymentRepository.save(payment);
    }

    // 2. Thanh toán thành công (ONLINE)
    public void handlePaymentSuccess(Integer paymentID) {

    Payment payment = paymentRepository.findById(paymentID)
            .orElseThrow();

    // tránh success nhiều lần
    if (payment.getStatus() != PaymentStatus.PENDING) {
        return;
    }

    Order order = orderRepository.findById(payment.getOrderID())
            .orElseThrow();

    // tránh trừ kho nhiều lần
    if (order.getStatus() != OrderStatus.CONFIRMED) {

        // ===== TRỪ STOCK =====
        deductStock(order.getOrderID());

        // ===== CONFIRM ORDER =====
        order.setStatus(OrderStatus.CONFIRMED);
    }

    payment.setStatus(PaymentStatus.SUCCESS);

    payment.setPaymentDate(LocalDateTime.now());

    payment.setTransactionCode(
            "TXN" + System.currentTimeMillis()
    );

    payment.setNote("Thanh toán thành công");

    paymentRepository.save(payment);

    orderRepository.save(order);

    // ===== CỘNG TOTAL SPENT =====
    syncUserTotalSpent(order.getUserID());

    // ===== UPDATE MEMBERSHIP =====
    membershipLevelService.updateUserMembershipLevel(
            order.getUserID()
    );
}

    // 3. Thanh toán thất bại (ONLINE)
    public void handlePaymentFail(Integer paymentID) {

        Payment payment = paymentRepository.findById(paymentID)
                .orElseThrow();

        if(payment.getStatus() != PaymentStatus.PENDING) return;

        payment.setStatus(PaymentStatus.FAILED);
        payment.setNote("Thanh toán thất bại");

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

    private void syncUserTotalSpent(Integer userID) {

        BigDecimal totalSpent =
                orderRepository.calculateTotalSpentByUserID(
                        userID
                );

        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }

        User user = userRepository.findById(userID)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: "
                                        + userID
                        )
                );

        user.setTotalSpent(totalSpent);

        userRepository.save(user);
    }

    //Lấy tất cả payment
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
                                new ResourceNotFoundException("Order not found"));

                User user = userRepository.findById(order.getUserID())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("User not found"));

                AdminPaymentResponse response =
                        new AdminPaymentResponse();

                response.setPaymentID(payment.getPaymentID());

                response.setPaymentCode(payment.getPaymentCode());

                response.setOrderID(order.getOrderID());

                response.setOrderCode(order.getOrderCode());

                response.setCustomerName(user.getFullName());

                response.setMethod(payment.getMethod().name());

                response.setStatus(payment.getStatus().name());

                response.setOrderStatus(
                        order.getStatus().name());

                response.setAmount(payment.getAmount());

                response.setPaymentDate(payment.getPaymentDate());

                response.setTransactionCode(
                        payment.getTransactionCode());

                response.setNote(payment.getNote());

                return response;

        }).toList();
        }
        //Lấy chi tiết 1 payment
public AdminPaymentResponse getPaymentById(Integer id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment not found"));

        Order order = orderRepository.findById(payment.getOrderID())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found"));

        User user = userRepository.findById(order.getUserID())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        AdminPaymentResponse response =
                new AdminPaymentResponse();

        response.setPaymentID(payment.getPaymentID());
                response.setPaymentCode(
        payment.getPaymentCode());

        response.setOrderID(order.getOrderID());
        response.setOrderCode(
                order.getOrderCode());

        response.setCustomerName(user.getFullName());

        response.setMethod(payment.getMethod().name());

        response.setStatus(payment.getStatus().name());

        response.setAmount(payment.getAmount());

        response.setPaymentDate(payment.getPaymentDate());

        response.setTransactionCode(
                payment.getTransactionCode());

        response.setOrderStatus(
                order.getStatus().name());

        response.setNote(
                payment.getNote());

        return response;
        }

        private void deductStock(Integer orderID) {

    List<OrderDetail> details =
            orderDetailRepository.findByOrderID(orderID);

    for (OrderDetail detail : details) {

        Product product =
                productRepository.findByIdForUpdate(
                        detail.getProductID()
                );

        if (product == null) {
            throw new ResourceNotFoundException(
                    "Product not found"
            );
        }

        if (product.getQuantity() < detail.getQuantity()) {

            throw new OutOfStockException(
                    product.getName() + " out of stock"
            );
        }

        product.setQuantity(
                product.getQuantity()
                        - detail.getQuantity()
        );

        productRepository.save(product);
    }
}
}
