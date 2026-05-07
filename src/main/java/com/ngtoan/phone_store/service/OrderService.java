package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.CheckoutItemRequest;
import com.ngtoan.phone_store.dto.request.CheckoutRequest;
import com.ngtoan.phone_store.dto.response.OrderAdminResponse;
import com.ngtoan.phone_store.dto.response.OrderDetailResponse;
import com.ngtoan.phone_store.dto.response.OrderResponse;
import com.ngtoan.phone_store.dto.response.PaymentResponse;
import com.ngtoan.phone_store.entity.*;
import com.ngtoan.phone_store.exception.BadRequestException;
import com.ngtoan.phone_store.exception.OutOfStockException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

        // ===== PAYMENT =====
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    // ===== MEMBERSHIP =====
    private final MembershipLevelService membershipLevelService;

    // ===== CHECKOUT =====
    public OrderResponse placeOrder(String username, CheckoutRequest request) {

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        Integer userID = user.getUserId();

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("No items selected");
        }

        // ===== VALIDATE PAYMENT METHOD =====
        PaymentMethod method;
        try {
            method = request.getPaymentMethod() == null
                    ? PaymentMethod.COD
                    : PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid payment method");
        }

        BigDecimal total = BigDecimal.ZERO;

        // ===== CHECK STOCK + TÍNH TOTAL =====
        for (CheckoutItemRequest item : request.getItems()) {

            Product product = productRepository.findByIdForUpdate(item.getProductID());

            if (product == null) {
                throw new ResourceNotFoundException(
                        "Product not found with id: " + item.getProductID()
                );
            }

            if (item.getQuantity() <= 0) {
                throw new BadRequestException("Quantity must be greater than 0");
            }

            if (product.getQuantity() < item.getQuantity()) {
                throw new OutOfStockException(product.getName() + " out of stock");
            }

            BigDecimal price = product.getPromotionPrice() != null
                    ? product.getPromotionPrice()
                    : product.getPrice();

            total = total.add(
                    price.multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }

        // ===== TẠO ORDER =====
        Order order = Order.builder()
                .userID(userID)
                .orderCode(generateOrderCode())
                .createdDate(LocalDateTime.now())
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .customerName(request.getCustomerName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .paymentMethod(method) // ✅ dùng enum
                .build();

        orderRepository.save(order);

        // ===== TẠO PAYMENT =====
        paymentService.createPayment(order.getOrderID(), method);

        // ===== TẠO ORDER DETAIL + TRỪ KHO =====
        for (CheckoutItemRequest item : request.getItems()) {

            Product product = productRepository.findByIdForUpdate(item.getProductID());

            BigDecimal price = product.getPromotionPrice() != null
                    ? product.getPromotionPrice()
                    : product.getPrice();

            BigDecimal subtotal = price.multiply(
                    BigDecimal.valueOf(item.getQuantity())
            );

            // trừ kho
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);

            OrderDetail detail = OrderDetail.builder()
                    .orderID(order.getOrderID())
                    .productID(product.getProductID())
                    .productName(product.getName())
                    .image(product.getImage())
                    .quantity(item.getQuantity())
                    .price(price)
                    .subtotal(subtotal)
                    .build();

            orderDetailRepository.save(detail);
        }

        // ===== CLEAR CART =====
        clearPurchasedItemsFromCart(userID, request);

        // ===== RESPONSE =====
        OrderResponse response = new OrderResponse();
        response.setOrderID(order.getOrderID());
        response.setOrderCode(order.getOrderCode());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().name());
        response.setCreatedDate(order.getCreatedDate());

        return response;
    }

    // ===== ADMIN UPDATE ORDER =====
    public OrderAdminResponse updateOrderStatus(Integer orderID, OrderStatus status) {

        Order order = orderRepository.findById(orderID)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderID));

        order.setStatus(status);
        orderRepository.save(order);

        // 🔥 Sync payment (COD only)
        paymentService.syncPaymentWithOrder(order);

        // Đồng bộ totalSpent
        syncUserTotalSpent(order.getUserID());

        // ===== UPDATE MEMBERSHIP =====
        membershipLevelService.updateUserMembershipLevel(
                order.getUserID()
        );

        return toAdminResponse(order);
    }

    // ===== CLEAR CART =====
    private void clearPurchasedItemsFromCart(Integer userID, CheckoutRequest request) {

        Cart cart = cartRepository.findByUserID(userID).orElse(null);
        if (cart == null) return;

        for (CheckoutItemRequest item : request.getItems()) {
            cartItemRepository
                    .findByCartIDAndProductID(cart.getCartID(), item.getProductID())
                    .ifPresent(cartItemRepository::delete);
        }
    }

    // ===== GEN ORDER CODE =====
    private String generateOrderCode() {
        return "OD" + System.currentTimeMillis();
    }

    // ===== ADMIN VIEW =====
    public List<OrderAdminResponse> getAllOrdersForAdmin() {
        return orderRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                .map(this::toAdminResponse)
                .toList();
    }

    public OrderAdminResponse getOrderDetailForAdmin(Integer orderID) {
        Order order = orderRepository.findById(orderID)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderID));

        return toAdminResponse(order);
    }

    // ===== USER VIEW =====
    public List<OrderAdminResponse> getMyOrders(String username) {

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        return orderRepository.findByUserID(user.getUserId())
                .stream()
                .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                .map(this::toAdminResponse)
                .toList();
    }

    // ===== MAP RESPONSE =====
  private OrderAdminResponse toAdminResponse(Order order) {

    List<OrderDetail> details =
            orderDetailRepository.findByOrderID(order.getOrderID());

    List<OrderDetailResponse> itemResponses = details.stream()
            .map(detail -> {
                OrderDetailResponse item = new OrderDetailResponse();
                item.setOrderDetailID(detail.getOrderDetailID());
                item.setProductID(detail.getProductID());
                item.setProductName(detail.getProductName());
                item.setImage(detail.getImage());
                item.setQuantity(detail.getQuantity());
                item.setPrice(detail.getPrice());
                item.setSubtotal(detail.getSubtotal());
                return item;
            })
            .toList();

    OrderAdminResponse response = new OrderAdminResponse();
    response.setOrderID(order.getOrderID());
    response.setUserID(order.getUserID());
    response.setOrderCode(order.getOrderCode());
    response.setCreatedDate(order.getCreatedDate());
    response.setTotalAmount(order.getTotalAmount());
    response.setStatus(order.getStatus().name());
    response.setCustomerName(order.getCustomerName());
    response.setPhone(order.getPhone());
    response.setAddress(order.getAddress());
    response.setPaymentMethod(order.getPaymentMethod().name());
    response.setItems(itemResponses);

    // ===== PAYMENT =====
    Payment payment = paymentRepository.findByOrderID(order.getOrderID())
            .orElse(null);

    if (payment != null) {
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setPaymentID(payment.getPaymentID());
        paymentResponse.setMethod(payment.getMethod().name());
        paymentResponse.setStatus(payment.getStatus().name());
        paymentResponse.setAmount(payment.getAmount());
        paymentResponse.setPaymentDate(payment.getPaymentDate());
        paymentResponse.setTransactionCode(payment.getTransactionCode());

        response.setPayment(paymentResponse);
    } else {
        response.setPayment(null); // optional
    }

    // ===== RETURN CUỐI =====
    return response;
}

    // ===== SYNC USER TOTAL =====
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
                        new ResourceNotFoundException(
                                "User not found with id: "
                                        + userID
                        )
                );

        user.setTotalSpent(totalSpent);

        userRepository.save(user);
    }
}