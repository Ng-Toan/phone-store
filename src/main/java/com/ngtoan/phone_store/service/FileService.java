package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class FileService {

    private static final String UPLOAD_DIR = System.getProperty("user.dir")
            + File.separator + "src"
            + File.separator + "main"
            + File.separator + "resources"
            + File.separator + "static"
            + File.separator + "img"
            + File.separator + "upload"
            + File.separator;

    public String uploadProductImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File không được để trống");
        }

        try {
            String originalName = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + originalName;

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
}