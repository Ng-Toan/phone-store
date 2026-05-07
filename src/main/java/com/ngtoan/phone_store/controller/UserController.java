package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.service.EmailVerificationService;
import com.ngtoan.phone_store.service.UserService;
import com.ngtoan.phone_store.dto.request.ResendVerificationRequest;
import com.ngtoan.phone_store.dto.request.UserCreationRequest;
import com.ngtoan.phone_store.dto.request.UserUpdateRequest;
import com.ngtoan.phone_store.dto.request.VerifyEmailRequest;
import com.ngtoan.phone_store.dto.response.UserProfileResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Map;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailVerificationService emailVerificationService;

    // đăng ký tạm và gửi OTP
    @PostMapping("/register")
    public String register(@Valid @RequestBody UserCreationRequest request) {
        return userService.register(request);
    }

    // xác thực email bằng OTP
    @PostMapping("/verify-email")
    public String verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return emailVerificationService.verifyEmail(
                request.getEmail(),
                request.getOtpCode()
        );
    }

    // gửi lại mã OTP
    @PostMapping("/resend-verification")
    public String resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        return emailVerificationService.resendVerificationCode(request.getEmail());
    }

    // user xem profile
    @GetMapping("/profile")
    public User getProfile(Authentication authentication) {

        String username = authentication.getName();

        return userService.findByUsername(username);
    }

    // user update profile
    @PutMapping("/profile")
    public User updateProfile(Authentication authentication,
                              @Valid @RequestBody UserUpdateRequest request) {

        String username = authentication.getName();

        User user = userService.findByUsername(username);

        return userService.updateUser(user.getUserId(), request);
    }

    // user đổi password
    @PutMapping("/change-password")
    public String changePassword(Authentication authentication,
                                 @RequestBody Map<String,String> req) {

        String username = authentication.getName();

        return userService.changePassword(
                username,
                req.get("oldPassword"),
                req.get("newPassword")
        );
    }

    @GetMapping("/me")
    public UserProfileResponse getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        return userService.getMyProfile(username);
    }
}