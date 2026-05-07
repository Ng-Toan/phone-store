package com.ngtoan.phone_store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationOtp(String toEmail, String otpCode) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Mã xác thực tài khoản CellCentral");

        message.setText(
                "Xin chào,\n\n"
                + "Mã xác thực tài khoản CellCentral của bạn là: " + otpCode + "\n\n"
                + "Mã này có hiệu lực trong 5 phút.\n"
                + "Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\n"
                + "Trân trọng,\n"
                + "CellCentral"
        );

        mailSender.send(message);
    }
}