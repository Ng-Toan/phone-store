package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.dto.request.ForgotPasswordRequest;
import com.ngtoan.phone_store.dto.request.ResetPasswordRequest;
import com.ngtoan.phone_store.service.PasswordResetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    // User nhập email để nhận OTP đặt lại mật khẩu
    @PostMapping("/forgot")
    public String forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return passwordResetService.forgotPassword(request.getEmail());
    }

    // User nhập OTP + mật khẩu mới
    @PostMapping("/reset")
    public String resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return passwordResetService.resetPassword(request);
    }
}