package com.signly.common.storage;

import com.signly.common.storage.strategy.LocalFileStorageStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileStorageService {

    private final FileStorageStrategy strategy;

    public FileStorageService(
            @Value("${app.storage.type:local}") String storageType,
            LocalFileStorageStrategy localFileStorageStrategy
    ) {
        this.strategy = createStrategy(storageType, localFileStorageStrategy);
    }

    private FileStorageStrategy createStrategy(String storageType, LocalFileStorageStrategy localFileStorageStrategy) {
        return switch (storageType.toLowerCase()) {
            case "local" -> localFileStorageStrategy;
            // 향후 다른 저장소 타입 추가 가능
            // case "s3" -> s3FileStorageStrategy;
            default -> localFileStorageStrategy;
        };
    }

    public StoredFile storeFile(
            MultipartFile file,
            String category
    ) {
        try {
            return strategy.storeFile(
                    file.getBytes(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    category
            );
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다", e);
        }
    }

    public StoredFile storeFile(
            byte[] data,
            String originalFilename,
            String contentType,
            String category
    ) {
        return strategy.storeFile(data, originalFilename, contentType, category);
    }

    public byte[] loadFile(String filePath) {
        return strategy.loadFile(filePath);
    }

    public void deleteFile(String filePath) {
        strategy.deleteFile(filePath);
    }

    public boolean fileExists(String filePath) {
        return strategy.fileExists(filePath);
    }
}
