package com.ngtoan.phone_store.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ngtoan.phone_store.dto.response.AdminPaymentResponse;
import com.ngtoan.phone_store.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ADMIN - GET ALL PAYMENTS
    @GetMapping
    public List<AdminPaymentResponse> getAllPayments() {
        return paymentService.getAllPayments();
    }

    // ADMIN - GET PAYMENT DETAIL
    @GetMapping("/{id}")
    public AdminPaymentResponse getPaymentById(
            @PathVariable Integer id) {

        return paymentService.getPaymentById(id);
    }

    // PAYMENT SUCCESS
    @PostMapping("/{id}/success")
    public String success(@PathVariable Integer id) {

        paymentService.handlePaymentSuccess(id);

        return "Payment success";
    }

    // PAYMENT FAIL
    @PostMapping("/{id}/fail")
    public String fail(@PathVariable Integer id) {

        paymentService.handlePaymentFail(id);

        return "Payment failed";
    }
}