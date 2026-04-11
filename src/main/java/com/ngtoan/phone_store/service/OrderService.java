package com.ngtoan.phone_store.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ngtoan.phone_store.dto.response.OrderResponse;
import com.ngtoan.phone_store.entity.*;
import com.ngtoan.phone_store.exception.*;
import com.ngtoan.phone_store.mapper.OrderMapper;
import com.ngtoan.phone_store.repository.*;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    private final ProductRepository productRepository;

    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse placeOrder(Integer userID) {

        // 1. Lấy cart
        Cart cart = cartRepository.findByUserID(userID)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        List<CartItem> items = cartItemRepository.findByCartID(cart.getCartID());

        if(items.isEmpty()){
            throw new BadRequestException("Cart is empty");
        }

        // 2. Check stock + tính total
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : items) {
            Product product = productRepository.findByIdForUpdate(item.getProductID());

            if(product == null){
                throw new ResourceNotFoundException("Product not found");
            }

            if(product.getQuantity() < item.getQuantity()){
                throw new OutOfStockException("Product hết hàng: " + product.getName());
            }

            total = total.add(product.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // 3. Tạo Order
        Order order = Order.builder()
                .userID(userID)
                .createdDate(LocalDateTime.now())
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .build();

        orderRepository.save(order);

        // 4. Tạo OrderDetail + trừ stock
        for (CartItem item : items) {

            Product product = productRepository.findByIdForUpdate(item.getProductID());

            // trừ stock
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);

            // tạo detail
            OrderDetail detail = OrderDetail.builder()
                    .orderID(order.getOrderID())
                    .productID(product.getProductID())
                    .quantity(item.getQuantity())
                    .price(product.getPrice())
                    .build();

            orderDetailRepository.save(detail);
        }

        // 5. Xóa cart item
        cartItemRepository.deleteAll(items);

        // 6. return DTO
        return orderMapper.toDTO(order);
    }
}
