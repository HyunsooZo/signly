package com.signly.signature.domain.model;

import com.signly.common.domain.AggregateRoot;
import com.signly.common.exception.ValidationException;
import com.signly.document.domain.model.FileMetadata;
import com.signly.user.domain.model.UserId;

import java.time.LocalDateTime;

public class FirstPartySignature extends AggregateRoot {

    private final SignatureId signatureId;
    private final UserId ownerId;
    private FileMetadata fileMetadata;
    private String storagePath;

    private FirstPartySignature(SignatureId signatureId,
                               UserId ownerId,
                               FileMetadata fileMetadata,
                               String storagePath,
                               LocalDateTime createdAt,
                               LocalDateTime updatedAt) {
        super(createdAt, updatedAt);
        this.signatureId = signatureId;
        this.ownerId = ownerId;
        this.fileMetadata = fileMetadata;
        this.storagePath = storagePath;
    }

    public static FirstPartySignature create(UserId ownerId,
                                             FileMetadata fileMetadata,
                                             String storagePath) {
        validateOwner(ownerId);
        validateStoragePath(storagePath);

        return new FirstPartySignature(
                SignatureId.generate(),
                ownerId,
                fileMetadata,
                storagePath,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static FirstPartySignature restore(SignatureId signatureId,
                                              UserId ownerId,
                                              FileMetadata fileMetadata,
                                              String storagePath,
                                              LocalDateTime createdAt,
                                              LocalDateTime updatedAt) {
        validateOwner(ownerId);
        validateStoragePath(storagePath);

        return new FirstPartySignature(
                signatureId,
                ownerId,
                fileMetadata,
                storagePath,
                createdAt,
                updatedAt
        );
    }

    public void updateFile(FileMetadata fileMetadata, String storagePath) {
        validateStoragePath(storagePath);
        this.fileMetadata = fileMetadata;
        this.storagePath = storagePath;
        updateTimestamp();
    }

    public SignatureId getSignatureId() {
        return signatureId;
    }

    public UserId getOwnerId() {
        return ownerId;
    }

    public FileMetadata getFileMetadata() {
        return fileMetadata;
    }

    public String getStoragePath() {
        return storagePath;
    }

    private static void validateOwner(UserId ownerId) {
        if (ownerId == null) {
            throw new ValidationException("소유자 정보가 필요합니다");
        }
    }

    private static void validateStoragePath(String storagePath) {
        if (storagePath == null || storagePath.trim().isEmpty()) {
            throw new ValidationException("서명 파일 경로가 유효하지 않습니다");
        }
    }
}
