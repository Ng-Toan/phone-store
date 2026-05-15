package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.LoginRequest;
import com.ngtoan.phone_store.dto.request.UserCreationRequest;
import com.ngtoan.phone_store.dto.request.UserUpdateRequest;
import com.ngtoan.phone_store.dto.response.UserProfileResponse;
import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.exception.BadRequestException;
import com.ngtoan.phone_store.exception.ForbiddenException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.exception.UnauthorizedException;
import com.ngtoan.phone_store.repository.UserRepository;
import com.ngtoan.phone_store.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Validated
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    // =========================================================
    // USER FUNCTIONS
    // =========================================================

    // Register: chỉ lưu tạm, chưa tạo User thật
    public String register(@Valid @RequestBody UserCreationRequest dto) {
        return emailVerificationService.createPendingRegistration(dto);
    }

    // Login JWT
    public String login(LoginRequest dto) {

        User user = userRepository.findByUsername(dto.getUsername());

        if (user == null) {
            throw new UnauthorizedException("Invalid username or password");
        }

        // Bắt buộc tên đăng nhập phải đúng chữ hoa/thường như lúc đăng ký
        if (!user.getUsername().equals(dto.getUsername())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
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

    // Get user by id
    public User getUserById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id));
    }

    // User update profile
    // User chỉ được sửa: fullName, phone, gender, birthDate/birthday, address/defaultAddress.
    // Email không được sửa vì email đã xác thực lúc tạo tài khoản.
    // Không cho user tự sửa roleId, status, levelId, totalSpent.
    public User updateUser(int id, UserUpdateRequest dto) {

        User user = getUserById(id);

        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName().trim());
        }

        // Không update email ở profile user, kể cả frontend cố tình gửi email lên.

        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone().trim());
        }

        if (dto.getGender() != null) {
            user.setGender(dto.getGender().trim());
        }

        LocalDate newBirthDate = dto.getBirthday() != null
                ? dto.getBirthday()
                : dto.getBirthDate();

        if (newBirthDate != null) {
            user.setBirthDate(newBirthDate);
        }

        String newAddress = dto.getDefaultAddress() != null
                ? dto.getDefaultAddress()
                : dto.getAddress();

        if (newAddress != null) {
            user.setAddress(newAddress.trim());
        }

        return userRepository.save(user);
    }

    // Find by username
    public User findByUsername(String username) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ResourceNotFoundException(
                    "User not found with username: " + username
            );
        }

        return user;
    }

    // User profile response
    public UserProfileResponse getMyProfile(String username) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        return toUserProfileResponse(user);
    }

    public UserProfileResponse toUserProfileResponse(User user) {

        UserProfileResponse response = new UserProfileResponse();

        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setGender(user.getGender());

        // Trả cả 2 kiểu tên để frontend cũ/mới đều đọc được
        response.setBirthDate(user.getBirthDate());
        response.setBirthday(user.getBirthDate());
        response.setAddress(user.getAddress());
        response.setDefaultAddress(user.getAddress());

        response.setRoleId(user.getRoleId());
        response.setLevelId(user.getLevelId());

        response.setTotalSpent(
                user.getTotalSpent() == null
                        ? BigDecimal.ZERO
                        : user.getTotalSpent()
        );

        response.setStatus(user.getStatus());
        response.setCreatedDate(user.getCreatedDate());

        return response;
    }
}
