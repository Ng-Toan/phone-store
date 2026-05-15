package com.ngtoan.phone_store.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.exception.UnauthorizedException;
import com.ngtoan.phone_store.repository.UserRepository;
import com.ngtoan.phone_store.util.JwtUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;

    @Value("${google.client-id}")
    private String googleClientId;

    public Map<String, Object> loginWithGoogle(String idTokenString) {
        try {
            GoogleIdToken idToken = verifyGoogleToken(idTokenString);

            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String fullName = (String) payload.get("name");

            if (email == null || email.isBlank()) {
                throw new UnauthorizedException("Không lấy được email từ tài khoản Google");
            }

            User user = userRepository.findByEmail(email);

            if (user == null) {
                user = createGoogleUser(email, fullName);
            }

            if (Boolean.FALSE.equals(user.getStatus())) {
                throw new UnauthorizedException("Tài khoản đã bị khóa");
            }

            String roleName = user.getRoleId() == 1 ? "ADMIN" : "USER";
            String token = JwtUtil.generateToken(user.getUsername());

            Map<String, Object> res = new HashMap<>();
            res.put("token", token);
            res.put("username", user.getUsername());
            res.put("fullName", user.getFullName());
            res.put("userId", user.getUserId());
            res.put("email", user.getEmail());
            res.put("phone", user.getPhone());
            res.put("role", roleName);

            return res;

        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException(
                    "Đăng nhập Google thất bại: " + e.getClass().getSimpleName()
            );
        }
    }

    private GoogleIdToken verifyGoogleToken(String idTokenString) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null) {
            throw new UnauthorizedException("Google token không hợp lệ hoặc sai clientId");
        }

        return idToken;
    }

    private User createGoogleUser(String email, String fullName) {
        User user = new User();

        user.setUsername(email);
        user.setEmail(email);
        user.setFullName(
                fullName != null && !fullName.isBlank()
                        ? fullName
                        : email
        );

        user.setPassword(UUID.randomUUID().toString());
        user.setPhone("Chưa cập nhật");
        user.setRoleId(2); // USER
        user.setStatus(true);
        user.setTotalSpent(BigDecimal.ZERO);
        user.setCreatedDate(LocalDateTime.now());

        return userRepository.save(user);
    }
}