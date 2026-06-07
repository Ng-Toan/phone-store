package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.ResetPasswordRequest;
import com.ngtoan.phone_store.entity.PasswordResetOtp;
import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.exception.BadRequestException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.PasswordResetOtpRepository;
import com.ngtoan.phone_store.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int OTP_EXPIRE_MINUTES = 5;

    // User nhập email, hệ thống gửi OTP nếu email tồn tại
    public String forgotPassword(String email) {

        User user = userRepository.findByEmail(email);

        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new ResourceNotFoundException("Email does not exist");
        }

        if (!Boolean.TRUE.equals(user.getStatus())) {
            throw new BadRequestException("Account is locked or not verified");
        }

        String otpCode = generateOtpCode();

        passwordResetOtpRepository.deleteByEmail(email);

        PasswordResetOtp resetOtp = new PasswordResetOtp();
        resetOtp.setEmail(email);
        resetOtp.setOtpCode(otpCode);
        resetOtp.setExpiredAt(LocalDateTime.now().plusMinutes(OTP_EXPIRE_MINUTES));
        resetOtp.setUsed(false);
        resetOtp.setCreatedAt(LocalDateTime.now());

        passwordResetOtpRepository.save(resetOtp);

        emailService.sendResetPasswordOtp(email, otpCode);

        return "Reset password code has been sent to your email";
    }

    // User nhập OTP + mật khẩu mới, đúng thì đổi password
    public String resetPassword(ResetPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail());

        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new ResourceNotFoundException("Email does not exist");
        }

        PasswordResetOtp resetOtp = passwordResetOtpRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No reset password request found with email: " + request.getEmail()
                ));

        if (resetOtp.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset password code has expired. Please request a new code.");
        }

        if (!resetOtp.getOtpCode().equals(request.getOtpCode())) {
            throw new BadRequestException("Invalid reset password code");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetOtp.setUsed(true);
        passwordResetOtpRepository.save(resetOtp);

        return "Password has been reset successfully";
    }

    private String generateOtpCode() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }
}