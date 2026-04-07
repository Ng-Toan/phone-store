package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.dto.request.AddToCartRequest;
import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.service.CartService;
import com.ngtoan.phone_store.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;   // thêm dòng này

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        User user = userService.findByUsername(username); // sửa chỗ này

        cartService.addToCart(user.getUserId(), request);

        return ResponseEntity.ok("Added to cart");
    }
}