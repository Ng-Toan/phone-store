package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;

@Service
public class FileService {

    private static final String UPLOAD_DIR =
            System.getProperty("user.dir")
                    + File.separator + "uploads"
                    + File.separator + "product"
                    + File.separator;

    public String uploadProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File không được để trống");
        }

        try {
            String originalName = file.getOriginalFilename();

            if (originalName == null || originalName.trim().isEmpty()) {
                throw new BadRequestException("Tên file không hợp lệ");
            }

            String fileName = System.currentTimeMillis() + "_" + cleanFileName(originalName);

            File directory = new File(UPLOAD_DIR);
            if (!directory.exists() && !directory.mkdirs()) {
                throw new BadRequestException("Không tạo được thư mục upload");
            }

            File saveFile = new File(directory, fileName);
            file.transferTo(saveFile);

            return "/img/upload/" + fileName;

        } catch (IOException e) {
            e.printStackTrace();
            throw new BadRequestException("Upload ảnh thất bại: " + e.getMessage());
        }
    }

    private String cleanFileName(String fileName) {
        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return normalized
                .replaceAll("[^a-zA-Z0-9\\.\\-_]", "-")
                .replaceAll("-+", "-");
    }
}