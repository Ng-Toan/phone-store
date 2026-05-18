package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.dto.request.UserUpdateRequest;
import com.ngtoan.phone_store.dto.response.UserAdminResponse;
import com.ngtoan.phone_store.dto.response.UserStatisticsResponse;
import com.ngtoan.phone_store.service.AdminUserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<UserAdminResponse>> getAllUsers() {
        return ResponseEntity.ok(
                adminUserService.getAllUsers()
        );
    }

    @GetMapping("/statistics")
    public ResponseEntity<UserStatisticsResponse> getStatistics() {
        return ResponseEntity.ok(
                adminUserService.getStatistics()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserAdminResponse>> searchUsers(
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(
                adminUserService.searchUsers(keyword)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAdminResponse> getUserById(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(
                adminUserService.getUserById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserAdminResponse> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(
                adminUserService.updateUser(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Integer id
    ) {
        adminUserService.deleteUser(id);

        return ResponseEntity.ok("User hidden successfully");
    }
}