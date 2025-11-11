package com.signly.document.infrastructure.storage;

import com.signly.common.exception.InfrastructureException;
import com.signly.document.domain.model.FileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(LocalFileStorageService.class);

    private final String uploadDir;

    public LocalFileStorageService(@Value("${app.file.upload-dir:./uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
        initializeUploadDirectory();
    }

    @Override
    public String storeFile(byte[] fileData, FileMetadata metadata) {
        try {
            String datePath = LocalDate.now().toString();
            String fileName = generateFileName(metadata.fileName());
            Path filePath = Paths.get(uploadDir, datePath, fileName);

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, fileData);

            String storagePath = datePath + "/" + fileName;
            logger.info("파일 저장 완료: {}", storagePath);
            return storagePath;

        } catch (IOException e) {
            logger.error("파일 저장 실패: {}", metadata.fileName(), e);
            throw new InfrastructureException("파일 저장에 실패했습니다", e);
        }
    }

    @Override
    public byte[] loadFile(String storagePath) {
        try {
            Path filePath = Paths.get(uploadDir, storagePath);

            if (!Files.exists(filePath)) {
                throw new InfrastructureException("파일을 찾을 수 없습니다: " + storagePath);
            }

            return Files.readAllBytes(filePath);

        } catch (IOException e) {
            logger.error("파일 로딩 실패: {}", storagePath, e);
            throw new InfrastructureException("파일 로딩에 실패했습니다", e);
        }
    }

    @Override
    public void deleteFile(String storagePath) {
        try {
            Path filePath = Paths.get(uploadDir, storagePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("파일 삭제 완료: {}", storagePath);
            }

        } catch (IOException e) {
            logger.error("파일 삭제 실패: {}", storagePath, e);
            throw new InfrastructureException("파일 삭제에 실패했습니다", e);
        }
    }

    @Override
    public boolean existsFile(String storagePath) {
        Path filePath = Paths.get(uploadDir, storagePath);
        return Files.exists(filePath);
    }

    private void initializeUploadDirectory() {
        try {
            Path path = Paths.get(uploadDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("업로드 디렉토리 생성: {}", uploadDir);
            }
        } catch (IOException e) {
            throw new InfrastructureException("업로드 디렉토리 생성에 실패했습니다: " + uploadDir, e);
        }
    }

    private String generateFileName(String originalFileName) {
        String extension = "";
        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFileName.substring(lastDotIndex);
        }
        return UUID.randomUUID().toString() + extension;
    }
}