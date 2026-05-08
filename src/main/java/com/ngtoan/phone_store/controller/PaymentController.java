package com.ngtoan.phone_store.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.ngtoan.phone_store.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    // 2. SUCCESS (fake gateway)
    @PostMapping("/{id}/success")
    public String success(@PathVariable Integer id) {
        paymentService.handlePaymentSuccess(id);
        return "Payment success";
    }

    // 3. FAIL (fake gateway)
    @PostMapping("/{id}/fail")
    public String fail(@PathVariable Integer id) {
        paymentService.handlePaymentFail(id);
        return "Payment failed";
    }
}
