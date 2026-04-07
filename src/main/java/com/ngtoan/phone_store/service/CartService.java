package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.AddToCartRequest;
import com.ngtoan.phone_store.entity.Cart;
import com.ngtoan.phone_store.entity.CartItem;
import com.ngtoan.phone_store.repository.CartItemRepository;
import com.ngtoan.phone_store.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public void addToCart(Integer userId, AddToCartRequest request) {

        Cart cart = cartRepository.findByUserID(userId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .userID(userId)
                                .createdDate(LocalDateTime.now())
                                .build()
                ));

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
    }
}