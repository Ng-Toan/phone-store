package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.service.ImportReceiptExcelService;
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
@RequestMapping("/admin/excel/imports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ImportReceiptExcelController {

    private final ImportReceiptExcelService importReceiptExcelService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportImports() {
        byte[] data = importReceiptExcelService.exportImports();

        return buildExcelResponse(data, "danh-sach-phieu-nhap.xlsx");
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] data = importReceiptExcelService.downloadTemplate();

        return buildExcelResponse(data, "mau-nhap-hang.xlsx");
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, String>> importImports(
            @RequestParam("file") MultipartFile file
    ) {
        String message = importReceiptExcelService.importImports(file);

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