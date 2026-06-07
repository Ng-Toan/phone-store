package com.ngtoan.phone_store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

        @Value("${brevo.api-key}")
        private String brevoApiKey;

        @Value("${brevo.sender-email}")
        private String senderEmail;

        @Value("${brevo.sender-name}")
        private String senderName;

        private final WebClient.Builder webClientBuilder;

        public void sendVerificationOtp(String toEmail, String otpCode) {

                Map<String, Object> body = Map.of(
                                "sender", Map.of(
                                                "name", senderName,
                                                "email", senderEmail),
                                "to", List.of(
                                                Map.of("email", toEmail)),
                                "subject", "Mã xác thực tài khoản CellCentral",
                                "htmlContent",
                                """
                                                <div style="font-family:Arial,sans-serif">
                                                    <h2>Xác thực tài khoản CellCentral</h2>
                                                    <p>Xin chào,</p>
                                                    <p>Mã xác thực tài khoản của bạn là:</p>
                                                    <h1 style="letter-spacing:4px">%s</h1>
                                                    <p>Mã này có hiệu lực trong 5 phút.</p>
                                                    <p>Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>
                                                    <br>
                                                    <p>Trân trọng,<br>CellCentral</p>
                                                </div>
                                                """.formatted(otpCode));

                webClientBuilder.build()
                                .post()
                                .uri("https://api.brevo.com/v3/smtp/email")
                                .header("api-key", brevoApiKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(body)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
        }

        public void sendResetPasswordOtp(String toEmail, String otpCode) {

                Map<String, Object> body = Map.of(
                                "sender", Map.of(
                                                "name", senderName,
                                                "email", senderEmail),
                                "to", List.of(
                                                Map.of("email", toEmail)),
                                "subject", "Mã đặt lại mật khẩu CellCentral",
                                "htmlContent",
                                """
                                                <div style="font-family:Arial,sans-serif">
                                                    <h2>Đặt lại mật khẩu CellCentral</h2>
                                                    <p>Xin chào,</p>
                                                    <p>Mã đặt lại mật khẩu của bạn là:</p>
                                                    <h1 style="letter-spacing:4px">%s</h1>
                                                    <p>Mã này có hiệu lực trong 5 phút.</p>
                                                    <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
                                                    <br>
                                                    <p>Trân trọng,<br>CellCentral</p>
                                                </div>
                                                """
                                                .formatted(otpCode));

                webClientBuilder.build()
                                .post()
                                .uri("https://api.brevo.com/v3/smtp/email")
                                .header("api-key", brevoApiKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(body)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
        }
}