package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.dto.request.AddToCartRequest;
import com.ngtoan.phone_store.dto.response.CartResponse;
import com.ngtoan.phone_store.service.CartService;
import com.ngtoan.phone_store.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    private Integer getUserId(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username).getUserId();
    }

    // 👉 ADD
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            @RequestBody AddToCartRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                cartService.addToCart(getUserId(authentication), request)
        );
    }

    // 👉 GET
    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {

        return ResponseEntity.ok(
                cartService.getCart(getUserId(authentication))
        );
    }

    // 👉 UPDATE
    @PutMapping("/update")
    public ResponseEntity<CartResponse> updateCart(
            @RequestParam Integer productId,
            @RequestParam Integer quantity,
            Authentication authentication) {

        return ResponseEntity.ok(
                cartService.updateCartItem(getUserId(authentication), productId, quantity)
        );
    }

    // 👉 REMOVE
    @DeleteMapping("/remove")
    public ResponseEntity<CartResponse> removeItem(
            @RequestParam Integer productId,
            Authentication authentication) {

        return ResponseEntity.ok(
                cartService.removeItem(getUserId(authentication), productId)
        );
    }

    // 👉 CLEAR
    @DeleteMapping("/clear")
    public ResponseEntity<CartResponse> clearCart(Authentication authentication) {

        return ResponseEntity.ok(
                cartService.clearCart(getUserId(authentication))
        );
    }
}