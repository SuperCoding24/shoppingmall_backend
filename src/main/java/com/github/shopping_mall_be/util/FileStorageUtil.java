package com.github.shopping_mall_be.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FileStorageUtil {

    @Value("${upload.dir}")
    private String uploadDir;

    // 최대 이미지 개수는 10개로 지정
    private static final int MAX_IMAGE_COUNT = 10;

    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

//        if (isMaxImageCountReached()) {
//            throw new IllegalStateException("Maximum image count reached");
//        }

        // Generate random file name to prevent duplicate file names
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path copyLocation = Paths.get(uploadDir + File.separator + fileName);
        Files.copy(file.getInputStream(), copyLocation);

        return fileName;
    }

    public List<String> storeMultipleFiles(List<MultipartFile> files) throws IOException {
        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                if (!isMaxImageCountReached()) {
                    String fileName = storeFile(file);
                    fileUrls.add(fileName);
                } else {
                    throw new IllegalStateException("Maximum image count reached");
                }
            }
        }
        return fileUrls;
    }

    private boolean isMaxImageCountReached() {
        File directory = new File(uploadDir);
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                return files.length >= MAX_IMAGE_COUNT;
            }
        }
        return false;
    }
}
