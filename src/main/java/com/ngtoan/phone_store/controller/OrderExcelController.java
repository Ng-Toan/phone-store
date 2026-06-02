package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.service.OrderExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/admin/excel/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OrderExcelController {

    private final OrderExcelService orderExcelService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportOrders() {
        byte[] data = orderExcelService.exportOrders();

        return buildExcelResponse(data, "danh-sach-don-hang.xlsx");
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