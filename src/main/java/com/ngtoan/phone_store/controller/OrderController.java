package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.dto.request.CheckoutRequest;
import com.ngtoan.phone_store.dto.response.OrderResponse;
import com.ngtoan.phone_store.service.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public OrderResponse checkout(
            Authentication authentication,
            @RequestBody CheckoutRequest request
    ) {
        String username = authentication.getName();

        return orderService.placeOrder(username, request);
    }
}