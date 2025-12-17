package com.signly.common.storage.strategy;

import com.signly.common.exception.ValidationException;
import com.signly.common.storage.FileStorageStrategy;
import com.signly.common.storage.StoredFile;
import com.signly.common.storage.util.FileSecurityValidator;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalFileStorageStrategy implements FileStorageStrategy {

    private final Path uploadPath;
    @Getter private final Set<String> allowedContentTypes;
    private final long maxFileSize;
    private final FileSecurityValidator fileSecurityValidator;

    public LocalFileStorageStrategy(
            @Value("${app.file.upload-dir:./uploads}") String uploadDir,
            @Value("${app.file.max-size:10485760}") long maxFileSize,
            FileSecurityValidator fileSecurityValidator
    ) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxFileSize = maxFileSize;
        this.fileSecurityValidator = fileSecurityValidator;
        this.allowedContentTypes = Set.of(
                "application/pdf",
                "image/jpeg",
                "image/png",
                "image/gif",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );

        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 디렉토리를 생성할 수 없습니다", e);
        }
    }

    @Override
    public StoredFile storeFile(
            byte[] data,
            String originalFilename,
            String contentType,
            String category
    ) {
        String sanitizedFilename = fileSecurityValidator.sanitizeFilename(originalFilename);
        validateFileBasics(sanitizedFilename, data.length);
        fileSecurityValidator.validateCategory(category);
        String validatedMimeType = fileSecurityValidator.detectAndValidateMimeType(data, contentType);
        String safeExtension = fileSecurityValidator.getSafeExtension(validatedMimeType);
        String storedFilename = generateStoredFilename(safeExtension);
        Path categoryPath = uploadPath.resolve(category);

        if (!categoryPath.normalize().startsWith(uploadPath)) {
            throw new ValidationException("잘못된 파일 경로입니다");
        }
        
        try {
            Files.createDirectories(categoryPath);
        } catch (IOException e) {
            throw new RuntimeException("카테고리 디렉토리를 생성할 수 없습니다", e);
        }

        Path targetLocation = categoryPath.resolve(storedFilename);
        
        if (!targetLocation.normalize().startsWith(uploadPath)) {
            throw new ValidationException("잘못된 파일 경로입니다");
        }

        try {
            Files.write(targetLocation, data);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다", e);
        }

        return new StoredFile(
                storedFilename,
                sanitizedFilename,
                category + "/" + storedFilename,
                validatedMimeType,  // 검증된 MIME 타입 사용
                data.length,
                LocalDateTime.now()
        );
    }

    @Override
    public byte[] loadFile(String filePath) {
        try {
            Path file = uploadPath.resolve(filePath).normalize();

            if (!file.startsWith(uploadPath)) {
                throw new ValidationException("잘못된 파일 경로입니다");
            }

            if (!Files.exists(file)) {
                throw new ValidationException("파일을 찾을 수 없습니다");
            }

            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new RuntimeException("파일 읽기 중 오류가 발생했습니다", e);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            Path file = uploadPath.resolve(filePath).normalize();

            if (!file.startsWith(uploadPath)) {
                throw new ValidationException("잘못된 파일 경로입니다");
            }

            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다", e);
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        Path file = uploadPath.resolve(filePath).normalize();
        return file.startsWith(uploadPath) && Files.exists(file);
    }

    private void validateFileBasics(
            String sanitizedFilename,
            long fileSize
    ) {
        if (fileSize <= 0) {
            throw new ValidationException("빈 파일은 업로드할 수 없습니다");
        }

        if (fileSize > maxFileSize) {
            throw new ValidationException("파일 크기가 너무 큽니다. 최대 " + (maxFileSize / 1024 / 1024) + "MB까지 가능합니다");
        }

        if (sanitizedFilename == null || sanitizedFilename.trim().isEmpty()) {
            throw new ValidationException("파일명이 유효하지 않습니다");
        }
    }

    private String generateStoredFilename(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + extension;
    }
}
