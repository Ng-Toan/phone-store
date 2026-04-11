package com.ngtoan.phone_store.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ngtoan.phone_store.dto.response.OrderResponse;
import com.ngtoan.phone_store.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public OrderResponse checkout(@RequestParam Integer userID){
        return orderService.placeOrder(userID);
    }
}
