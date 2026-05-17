package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.dto.request.UserUpdateRequest;
import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return "Deleted user with id: " + id;
    }
    @PutMapping("/users/{id}")
    public User adminUpdateUser(
            @PathVariable int id,
            @RequestBody UserUpdateRequest dto
    ) {
        return userService.adminUpdateUser(id, dto);
    }

}