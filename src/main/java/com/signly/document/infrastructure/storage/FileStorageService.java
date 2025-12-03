package com.signly.document.infrastructure.storage;

import com.signly.document.domain.model.FileMetadata;

public interface FileStorageService {
    String storeFile(
            byte[] fileData,
            FileMetadata metadata
    );

    byte[] loadFile(String storagePath);

    void deleteFile(String storagePath);

    boolean existsFile(String storagePath);
}