package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.dto.request.CheckoutRequest;
import com.ngtoan.phone_store.dto.response.OrderAdminResponse;
import com.ngtoan.phone_store.dto.response.OrderResponse;
import com.ngtoan.phone_store.entity.OrderStatus;
import com.ngtoan.phone_store.service.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // USER checkout
    @PostMapping("/checkout")
    public OrderResponse checkout(
            Authentication authentication,
            @RequestBody CheckoutRequest request
    ) {
        String username = authentication.getName();
        return orderService.placeOrder(username, request);
    }

    // ADMIN lấy tất cả đơn hàng
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderAdminResponse> getAllOrders() {
        return orderService.getAllOrdersForAdmin();
    }

    // ADMIN xem chi tiết 1 đơn
    @GetMapping("/admin/{orderID}")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderAdminResponse getOrderDetail(@PathVariable Integer orderID) {
        return orderService.getOrderDetailForAdmin(orderID);
    }

    // ADMIN cập nhật trạng thái đơn
    @PutMapping("/admin/{orderID}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderAdminResponse updateOrderStatus(
            @PathVariable Integer orderID,
            @RequestParam OrderStatus status
    ) {
        return orderService.updateOrderStatus(orderID, status);
    }
    // Profile USER
    @GetMapping("/my-orders")
    public List<OrderAdminResponse> getMyOrders(Authentication authentication) {
        String username = authentication.getName();
        return orderService.getMyOrders(username);
    }

    @PutMapping("/my-orders/{orderID}/cancel")
public OrderAdminResponse cancelMyOrder(
        Authentication authentication,
        @PathVariable Integer orderID
) {
    return orderService.cancelMyOrder(
            authentication.getName(),
            orderID
    );
}
}