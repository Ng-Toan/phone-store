package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.AddToCartRequest;
import com.ngtoan.phone_store.dto.response.CartItemResponse;
import com.ngtoan.phone_store.dto.response.CartResponse;
import com.ngtoan.phone_store.entity.Cart;
import com.ngtoan.phone_store.entity.CartItem;
import com.ngtoan.phone_store.entity.Product;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.CartItemRepository;
import com.ngtoan.phone_store.repository.CartRepository;
import com.ngtoan.phone_store.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    // 👉 ADD
    public CartResponse addToCart(Integer userId, AddToCartRequest request) {

        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cartItemRepository
                .findByCartIDAndProductID(cart.getCartID(), request.getProductId())
                .orElse(null);

        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            cartItem = CartItem.builder()
                    .cartID(cart.getCartID())
                    .productID(request.getProductId())
                    .quantity(request.getQuantity())
                    .build();
        }

        cartItemRepository.save(cartItem);

        return buildCartResponse(userId);
    }

    // 👉 GET
    public CartResponse getCart(Integer userId) {
        return buildCartResponse(userId);
    }

    // 👉 UPDATE
    public CartResponse updateCartItem(Integer userId, Integer productId, Integer quantity) {

        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository
                .findByCartIDAndProductID(cart.getCartID(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return buildCartResponse(userId);
    }

    // 👉 REMOVE
    public CartResponse removeItem(Integer userId, Integer productId) {

        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository
                .findByCartIDAndProductID(cart.getCartID(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cartItemRepository.delete(item);

        return buildCartResponse(userId);
    }

    // 👉 CLEAR
    public CartResponse clearCart(Integer userId) {

        Cart cart = getOrCreateCart(userId);

        cartItemRepository.deleteAllByCartID(cart.getCartID());

        return buildCartResponse(userId);
    }

    // =============================
    // 🔥 PRIVATE METHODS
    // =============================

    private Cart getOrCreateCart(Integer userId) {
        return cartRepository.findByUserID(userId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .userID(userId)
                                .createdDate(LocalDateTime.now())
                                .build()
                ));
    }

    private CartResponse buildCartResponse(Integer userId) {

        Cart cart = getOrCreateCart(userId);

        List<CartItem> items = cartItemRepository.findByCartID(cart.getCartID());

       List<CartItemResponse> itemResponses = items.stream().map(item -> {

            Product product = productRepository.findById(item.getProductID())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + item.getProductID()));
            BigDecimal price = product.getPromotionPrice() != null
            ? product.getPromotionPrice()
            : product.getPrice();

            CartItemResponse res = new CartItemResponse();
            res.setCartItemID(item.getCartItemID());
            res.setProductID(item.getProductID());
            res.setProductName(product.getName());
            res.setPrice(price);
            res.setQuantity(item.getQuantity());
            res.setSubtotal(price.multiply(BigDecimal.valueOf(item.getQuantity())));

            return res;

        }).toList();

        int totalQuantity = itemResponses.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        BigDecimal totalPrice = itemResponses.stream()
            .map(CartItemResponse::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartResponse response = new CartResponse();
        response.setCartID(cart.getCartID());
        response.setItems(itemResponses);
        response.setTotalQuantity(totalQuantity);
        response.setTotalPrice(totalPrice);

        return response;
    }
}