package com.deally.common.storage;

import java.time.LocalDateTime;

public record StoredFile(
        String storedFilename,
        String originalFilename,
        String filePath,
        String contentType,
        long size,
        LocalDateTime uploadedAt
) {

    public String getFileExtension() {
        if (originalFilename == null || originalFilename.lastIndexOf('.') == -1) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }

    public String getSizeInMB() {
        return String.format("%.2f", size / 1024.0 / 1024.0);
    }

    public boolean isPdf() {
        return "application/pdf".equals(contentType);
    }

    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }

    public boolean isDocument() {
        return contentType != null && (
                contentType.equals("application/msword") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        );
    }
}