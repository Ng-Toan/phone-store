package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.service.PaymentExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/admin/excel/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PaymentExcelController {

    private final PaymentExcelService paymentExcelService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportPayments() {
        byte[] data = paymentExcelService.exportPayments();

        return buildExcelResponse(data, "danh-sach-thanh-toan.xlsx");
    }

    private ResponseEntity<byte[]> buildExcelResponse(byte[] data, String filename) {
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename
                )
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(data);
    }
}