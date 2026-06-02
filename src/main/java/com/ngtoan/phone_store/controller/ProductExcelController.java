package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.service.ProductExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/products/excel")
@RequiredArgsConstructor
public class ProductExcelController {

    private final ProductExcelService productExcelService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportProducts() {
        byte[] data = productExcelService.exportProducts();

        return buildExcelResponse(data, "danh-sach-san-pham.xlsx");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] data = productExcelService.downloadTemplate();

        return buildExcelResponse(data, "mau-nhap-san-pham.xlsx");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    public ResponseEntity<Map<String, String>> importProducts(
            @RequestParam("file") MultipartFile file
    ) {
        String message = productExcelService.importProducts(file);

        return ResponseEntity.ok(Map.of("message", message));
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