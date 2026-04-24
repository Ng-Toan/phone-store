package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.CheckoutItemRequest;
import com.ngtoan.phone_store.dto.request.CheckoutRequest;
import com.ngtoan.phone_store.dto.response.OrderResponse;
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

    // ===== CHECKOUT BẰNG USERNAME TỪ JWT =====
    public OrderResponse placeOrder(String username, CheckoutRequest request) {

        User user = userRepository.findByUsername(username);

            if (user == null) {
                throw new ResourceNotFoundException("User not found");
            }

        Integer userID = user.getUserId();

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("No items selected");
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
                .paymentMethod(
                        request.getPaymentMethod() == null
                                ? "COD"
                                : request.getPaymentMethod()
                )
                .build();

        orderRepository.save(order);

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

        // ===== XÓA ITEM ĐÃ MUA TRONG CART =====
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

    // ===== CLEAR CART =====
    private void clearPurchasedItemsFromCart(Integer userID, CheckoutRequest request) {

        Cart cart = cartRepository.findByUserID(userID).orElse(null);

        if (cart == null) return;

        for (CheckoutItemRequest item : request.getItems()) {
            cartItemRepository
                    .findByCartIDAndProductID(
                            cart.getCartID(),
                            item.getProductID()
                    )
                    .ifPresent(cartItemRepository::delete);
        }
    }

    // ===== GEN ORDER CODE =====
    private String generateOrderCode() {
        return "OD" + System.currentTimeMillis();
    }
}