package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.repository.UserRepository;
import com.ngtoan.phone_store.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.ngtoan.phone_store.dto.request.LoginRequest;
import com.ngtoan.phone_store.dto.request.UserCreationRequest;
import com.ngtoan.phone_store.dto.request.UserUpdateRequest;
import com.ngtoan.phone_store.dto.response.UserProfileResponse;
import com.ngtoan.phone_store.exception.*;
import com.ngtoan.phone_store.mapper.UserMapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EmailVerificationService emailVerificationService;

    // =========================================================
    // USER FUNCTIONS
    // =========================================================

    // Register: chỉ lưu tạm, chưa tạo User thật
    public String register(@Valid @RequestBody UserCreationRequest dto) {
        return emailVerificationService.createPendingRegistration(dto);
    }

    // Login (JWT)
    public String login(LoginRequest dto) {

        User user = userRepository.findByUsername(dto.getUsername());

        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        if (!Boolean.TRUE.equals(user.getStatus())) {
            throw new ForbiddenException("Please verify your email before login");
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

    public UserProfileResponse getMyProfile(String username) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRoleId(user.getRoleId());
        response.setLevelId(user.getLevelId());
        response.setTotalSpent(user.getTotalSpent() == null ? BigDecimal.ZERO : user.getTotalSpent());
        response.setStatus(user.getStatus());
        response.setCreatedDate(user.getCreatedDate());

        return response;
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