package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.UserCreationRequest;
import com.ngtoan.phone_store.entity.PendingUserRegistration;
import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.exception.BadRequestException;
import com.ngtoan.phone_store.exception.DuplicateResourceException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.PendingUserRegistrationRepository;
import com.ngtoan.phone_store.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationService {

    private final PendingUserRegistrationRepository pendingRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int OTP_EXPIRE_MINUTES = 5;

    // Đăng ký tạm + gửi OTP
    public String createPendingRegistration(UserCreationRequest dto) {

        // Kiểm tra trong bảng User chính thức
        if (userRepository.findByUsername(dto.getUsername()) != null) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        // Nếu username này đang pending bởi email khác thì không cho dùng
        Optional<PendingUserRegistration> pendingByUsername =
                pendingRepository.findByUsername(dto.getUsername());

        if (pendingByUsername.isPresent()
                && !pendingByUsername.get().getEmail().equalsIgnoreCase(dto.getEmail())) {
            throw new DuplicateResourceException("Username is waiting for email verification");
        }

        String otpCode = generateOtpCode();

        // Nếu email này đã đăng ký tạm trước đó thì cập nhật lại thông tin + gửi OTP mới
        PendingUserRegistration pending = pendingRepository.findByEmail(dto.getEmail())
                .orElse(PendingUserRegistration.builder().build());

        pending.setUsername(dto.getUsername());
        pending.setPassword(passwordEncoder.encode(dto.getPassword()));
        pending.setEmail(dto.getEmail());
        pending.setFullName(dto.getFullName());
        pending.setPhone(dto.getPhone());
        pending.setRoleId(dto.getRoleId());
        pending.setOtpCode(otpCode);
        pending.setExpiredAt(LocalDateTime.now().plusMinutes(OTP_EXPIRE_MINUTES));
        pending.setCreatedAt(LocalDateTime.now());

        pendingRepository.save(pending);

        emailService.sendVerificationOtp(dto.getEmail(), otpCode);

        return "Registration information saved temporarily. Please check your email for verification code.";
    }

    // Xác thực OTP, đúng thì mới tạo User thật
    public String verifyEmail(String email, String otpCode) {

        PendingUserRegistration pending = pendingRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No pending registration found with email: " + email
                ));

        if (pending.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification code has expired. Please resend code.");
        }

        if (!pending.getOtpCode().equals(otpCode)) {
            throw new BadRequestException("Invalid verification code");
        }

        // Kiểm tra lại lần cuối trước khi tạo User chính thức
        if (userRepository.findByUsername(pending.getUsername()) != null) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (userRepository.existsByEmail(pending.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = new User();
        user.setUsername(pending.getUsername());
        user.setPassword(pending.getPassword());
        user.setEmail(pending.getEmail());
        user.setFullName(pending.getFullName());
        user.setPhone(pending.getPhone());
        user.setRoleId(pending.getRoleId());

        user.setLevelId(1);
        user.setTotalSpent(BigDecimal.ZERO);
        user.setStatus(true);
        user.setCreatedDate(LocalDateTime.now());

        userRepository.save(user);

        // Xác thực xong thì xóa bản ghi pending
        pendingRepository.delete(pending);

        return "Email verified successfully. Account has been created.";
    }

    // Gửi lại OTP
    public String resendVerificationCode(String email) {

        PendingUserRegistration pending = pendingRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No pending registration found with email: " + email
                ));

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email has already been verified");
        }

        String otpCode = generateOtpCode();

        pending.setOtpCode(otpCode);
        pending.setExpiredAt(LocalDateTime.now().plusMinutes(OTP_EXPIRE_MINUTES));
        pending.setCreatedAt(LocalDateTime.now());

        pendingRepository.save(pending);

        emailService.sendVerificationOtp(email, otpCode);

        return "Verification code has been resent";
    }

    private String generateOtpCode() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }
}