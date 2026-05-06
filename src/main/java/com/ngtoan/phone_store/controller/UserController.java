package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.service.UserService;
import com.ngtoan.phone_store.dto.request.UserCreationRequest;
import com.ngtoan.phone_store.dto.request.UserUpdateRequest;
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

    // đăng ký (public)
    @PostMapping("/register")
    public User register(@Valid @RequestBody UserCreationRequest request) {
        return userService.register(request);
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