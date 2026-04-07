package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.repository.UserRepository;
import com.ngtoan.phone_store.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.ngtoan.phone_store.dto.request.LoginRequest;
import com.ngtoan.phone_store.dto.request.UserCreationRequest;
import com.ngtoan.phone_store.dto.request.UserUpdateRequest;
import com.ngtoan.phone_store.exception.*;
import com.ngtoan.phone_store.mapper.UserMapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // =========================================================
    // USER FUNCTIONS
    // =========================================================

    // Register
    public User register(@Valid @RequestBody UserCreationRequest dto) {

        if (userRepository.findByUsername(dto.getUsername()) != null) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(true);
        System.out.println("DTO email: " + dto.getEmail());
        return userRepository.save(user);
    }

    // Login (JWT)
    public String login(LoginRequest dto) {

        User user = userRepository.findByUsername(dto.getUsername());

        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }
        if(!user.getStatus()){
            throw new ForbiddenException("Account is disabled");
        }

        return JwtUtil.generateToken(user.getUsername());
    }

    // Change password
    public String changePassword(String username, String oldPassword, String newPassword) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "Password changed successfully";
    }

    // Get profile
    public User getUserById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id));
    }

    // Update profile
    public User updateUser(int id, UserUpdateRequest dto) {

        User user = getUserById(id);

        userMapper.updateUser(user, dto);

        return userRepository.save(user);
    }

    // Find by username
    public User findByUsername(String username) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ResourceNotFoundException(
                    "User not found with username: " + username);
        }

        return user;
    }

    // =========================================================
    // ADMIN FUNCTIONS
    // =========================================================

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Search user by name
    public List<User> searchUserByName(String name) {

        List<User> users =
                userRepository.findByFullNameContainingIgnoreCase(name);

        if (users.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No users found with name: " + name);
        }

        return users;
    }

    // Admin update user
    public User adminUpdateUser(int id, UserUpdateRequest dto) {

        User user = getUserById(id);

        userMapper.updateUser(user, dto);

        if (dto.getRoleId() != null) {
            user.setRoleId(dto.getRoleId());
        }

        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }

        return userRepository.save(user);
    }

    // Delete user
    public void deleteUser(int id) {

        User user = getUserById(id);

        userRepository.delete(user);
    }
}