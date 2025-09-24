package com.signly.document.domain.model;

import com.signly.common.exception.ValidationException;

import java.util.Objects;

public class FileMetadata {
    private final String fileName;
    private final String mimeType;
    private final long fileSize;
    private final String checksum;

    private FileMetadata(String fileName, String mimeType, long fileSize, String checksum) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.checksum = checksum;
    }

    public static FileMetadata create(String fileName, String mimeType, long fileSize, String checksum) {
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

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getChecksum() {
        return checksum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileMetadata that = (FileMetadata) o;
        return fileSize == that.fileSize &&
                Objects.equals(fileName, that.fileName) &&
                Objects.equals(mimeType, that.mimeType) &&
                Objects.equals(checksum, that.checksum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, mimeType, fileSize, checksum);
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "fileName='" + fileName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", fileSize=" + fileSize +
                ", checksum='" + checksum + '\'' +
                '}';
    }
}