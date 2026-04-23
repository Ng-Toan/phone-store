package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload-product", consumes = "multipart/form-data")
    public String uploadProductImage(@RequestParam("file") MultipartFile file) {
        return fileService.uploadProductImage(file);
    }
}