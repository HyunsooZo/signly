package com.signly.common.storage;

import com.signly.common.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadPath;
    private final Set<String> allowedContentTypes;
    private final long maxFileSize;

    public FileStorageService(
            @Value("${app.file.upload-dir:./uploads}") String uploadDir,
            @Value("${app.file.max-size:10485760}") long maxFileSize) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxFileSize = maxFileSize;
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

    public StoredFile storeFile(MultipartFile file, String category) {
        validateFile(file.getOriginalFilename(), file.getContentType(), file.getSize());
        try {
            return storeFile(file.getBytes(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    category);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다", e);
        }
    }

    public StoredFile storeFile(byte[] data,
                               String originalFilename,
                               String contentType,
                               String category) {
        validateFile(originalFilename, contentType, data.length);

        String fileExtension = getFileExtension(originalFilename);
        String storedFilename = generateStoredFilename(fileExtension);

        Path categoryPath = uploadPath.resolve(category);
        try {
            Files.createDirectories(categoryPath);
        } catch (IOException e) {
            throw new RuntimeException("카테고리 디렉토리를 생성할 수 없습니다", e);
        }

        Path targetLocation = categoryPath.resolve(storedFilename);

        try {
            Files.write(targetLocation, data);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다", e);
        }

        return new StoredFile(
                storedFilename,
                originalFilename,
                category + "/" + storedFilename,
                contentType,
                data.length,
                LocalDateTime.now()
        );
    }

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

    public boolean fileExists(String filePath) {
        Path file = uploadPath.resolve(filePath).normalize();
        return file.startsWith(uploadPath) && Files.exists(file);
    }

    private void validateFile(String originalFilename, String contentType, long fileSize) {
        if (fileSize <= 0) {
            throw new ValidationException("빈 파일은 업로드할 수 없습니다");
        }

        if (fileSize > maxFileSize) {
            throw new ValidationException("파일 크기가 너무 큽니다. 최대 " + (maxFileSize / 1024 / 1024) + "MB까지 가능합니다");
        }

        if (contentType == null || !allowedContentTypes.contains(contentType)) {
            throw new ValidationException("지원하지 않는 파일 형식입니다");
        }

        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new ValidationException("파일명이 유효하지 않습니다");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    private String generateStoredFilename(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + extension;
    }
}
