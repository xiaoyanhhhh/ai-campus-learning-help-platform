package com.campus.aihelp.service;

import com.campus.aihelp.config.AppProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileStorageService {
    private final AppProperties properties;

    public FileStorageService(AppProperties properties) {
        this.properties = properties;
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Path dir = Path.of(properties.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
            String filename = UUID.randomUUID() + "-" + original.replaceAll("[\\\\/:*?\"<>|]", "_");
            file.transferTo(dir.resolve(filename));
            return "/uploads/" + filename;
        } catch (IOException ex) {
            throw new IllegalStateException("文件上传失败：" + ex.getMessage(), ex);
        }
    }
}
