package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.dto.request.LoginRequest;
import com.ngtoan.phone_store.service.UserService;
import com.ngtoan.phone_store.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {

        String token = userService.login(request);

        Map<String, String> res = new HashMap<>();
        res.put("token", token);

        return res;
    }
    @PostMapping("/test-token")
    public String testToken(@RequestBody String token) {
        return JwtUtil.extractUsername(token);
    }
}