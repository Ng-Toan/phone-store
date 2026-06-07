package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.LoginRequest;
import com.ngtoan.phone_store.dto.request.UserCreationRequest;
import com.ngtoan.phone_store.dto.request.UserUpdateRequest;
import com.ngtoan.phone_store.dto.response.UserProfileResponse;
import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.entity.MembershipLevel;
import com.ngtoan.phone_store.repository.MembershipLevelRepository;
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

@Service
@RequiredArgsConstructor
@Validated
public class UserService {

    private final UserRepository userRepository;
    private final MembershipLevelRepository membershipLevelRepository;
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

        User user = findLoginUser(dto.getUsername());

        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
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
            throw new ForbiddenException("Tài khoản chưa được xác thực hoặc đã bị khóa");
        }

        return JwtUtil.generateToken(user.getUsername());
    }

    // Change password
    public String changePassword(String username, String oldPassword, String newPassword) {

        User user = userRepository.findByUsername(username);

        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
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

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        return user;
    }

    // User update profile
    // Chỉ cho user sửa fullName, email, phone
    // Không cho user tự sửa roleId, status, levelId, totalSpent
    public User updateUser(int id, UserUpdateRequest dto) {

        User user = getUserById(id);

        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }

        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }

        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }

        if (dto.getBirthDate() != null) {
            user.setBirthDate(dto.getBirthDate());
        }

        if (dto.getAddress() != null) {
            user.setAddress(dto.getAddress());
        }

        return userRepository.save(user);
    }

    // Find by username
    public User findByUsername(String username) {

        User user = userRepository.findByUsername(username);

        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new ResourceNotFoundException(
                    "User not found with username: " + username);
        }

        return user;
    }

    // Find by username or email, dùng cho login
    public User findByUsernameOrEmail(String value) {

        User user = findLoginUser(value);

        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new ResourceNotFoundException("User not found");
        }

        return user;
    }

    private User findLoginUser(String value) {

        User user = userRepository.findByUsername(value);

        // Nếu là username thì bắt đúng chữ hoa/thường như lúc đăng ký
        if (user != null && !user.getUsername().equals(value)) {
            user = null;
        }

        if (user == null) {
            user = userRepository.findByEmail(value);
        }

        return user;
    }

    // User profile response
    public UserProfileResponse getMyProfile(String username) {

        User user = userRepository.findByUsername(username);

        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new ResourceNotFoundException("User not found");
        }

        BigDecimal totalSpent = user.getTotalSpent() == null
                ? BigDecimal.ZERO
                : user.getTotalSpent();

        MembershipLevel level = null;

        if (user.getLevelId() != null) {
            level = membershipLevelRepository.findById(user.getLevelId())
                    .orElse(null);
        }

        // Nếu user chưa có levelId thì tự lấy hạng phù hợp theo totalSpent
        if (level == null) {
            level = membershipLevelRepository
                    .findTopByMinSpentLessThanEqualOrderByMinSpentDesc(totalSpent)
                    .orElseGet(() -> membershipLevelRepository
                            .findTopByOrderByMinSpentAsc()
                            .orElse(null));
        }

        UserProfileResponse response = new UserProfileResponse();

        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setGender(user.getGender());
        response.setBirthDate(user.getBirthDate());
        response.setAddress(user.getAddress());
        response.setRoleId(user.getRoleId());

        response.setLevelId(level != null ? level.getLevelID() : user.getLevelId());
        response.setLevelName(level != null ? level.getLevelName() : "Đồng");
        response.setMinSpent(level != null ? level.getMinSpent() : BigDecimal.ZERO);
        response.setDiscountPercent(
                level != null ? level.getDiscountPercent() : BigDecimal.ZERO);

        response.setTotalSpent(totalSpent);
        response.setStatus(user.getStatus());
        response.setCreatedDate(user.getCreatedDate());

        return response;
    }
}