package com.deally.document.domain.model;

import com.deally.common.exception.ValidationException;

public record FileMetadata(
        String fileName,
        String mimeType,
        long fileSize,
        String checksum
) {
    public static FileMetadata create(
            String fileName,
            String mimeType,
            long fileSize,
            String checksum
    ) {
        validateFileName(fileName);
        validateMimeType(mimeType);
        validateFileSize(fileSize);
        validateChecksum(checksum);

        return new FileMetadata(fileName, mimeType, fileSize, checksum);
    }

    private static void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new ValidationException("파일명은 필수입니다");
        }
        if (fileName.length() > 255) {
            throw new ValidationException("파일명은 255자를 초과할 수 없습니다");
        }
    }

    private static void validateMimeType(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            throw new ValidationException("MIME 타입은 필수입니다");
        }
    }

    private static void validateFileSize(long fileSize) {
        if (fileSize < 0) {
            throw new ValidationException("파일 크기는 0 이상이어야 합니다");
        }
        if (fileSize > 100 * 1024 * 1024) { // 100MB
            throw new ValidationException("파일 크기는 100MB를 초과할 수 없습니다");
        }
    }

    private static void validateChecksum(String checksum) {
        if (checksum == null || checksum.trim().isEmpty()) {
            throw new ValidationException("체크섬은 필수입니다");
        }
    }
}