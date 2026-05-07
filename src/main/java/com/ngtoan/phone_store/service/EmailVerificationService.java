package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.entity.EmailVerificationToken;
import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.exception.BadRequestException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.EmailVerificationTokenRepository;
import com.ngtoan.phone_store.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private static final int OTP_EXPIRE_MINUTES = 5;

    public void createAndSendOtp(User user) {

        String otpCode = generateOtpCode();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .userID(user.getUserId())
                .email(user.getEmail())
                .otpCode(otpCode)
                .expiredAt(LocalDateTime.now().plusMinutes(OTP_EXPIRE_MINUTES))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        tokenRepository.save(token);

        emailService.sendVerificationOtp(user.getEmail(), otpCode);
    }

    public String verifyEmail(String email, String otpCode) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }

        if (Boolean.TRUE.equals(user.getStatus())) {
            return "Email has already been verified";
        }

        EmailVerificationToken token = tokenRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BadRequestException("Verification code not found"));

        if (token.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification code has expired");
        }

        if (!token.getOtpCode().equals(otpCode)) {
            throw new BadRequestException("Invalid verification code");
        }

        token.setUsed(true);
        tokenRepository.save(token);

        user.setStatus(true);
        userRepository.save(user);

        return "Email verified successfully";
    }

    public String resendVerificationCode(String email) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }

        if (Boolean.TRUE.equals(user.getStatus())) {
            return "Email has already been verified";
        }

        createAndSendOtp(user);

        return "Verification code has been resent";
    }

    private String generateOtpCode() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }
}