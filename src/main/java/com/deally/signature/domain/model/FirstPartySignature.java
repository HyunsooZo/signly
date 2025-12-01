package com.deally.signature.domain.model;

import com.deally.common.domain.AggregateRoot;
import com.deally.common.exception.ValidationException;
import com.deally.document.domain.model.FileMetadata;
import com.deally.user.domain.model.UserId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FirstPartySignature extends AggregateRoot {

    private final SignatureId signatureId;
    private final UserId ownerId;
    private FileMetadata fileMetadata;
    private String storagePath;

    private FirstPartySignature(
            SignatureId signatureId,
            UserId ownerId,
            FileMetadata fileMetadata,
            String storagePath,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        super(createdAt, updatedAt);
        this.signatureId = signatureId;
        this.ownerId = ownerId;
        this.fileMetadata = fileMetadata;
        this.storagePath = storagePath;
    }

    public static FirstPartySignature create(
            UserId ownerId,
            FileMetadata fileMetadata,
            String storagePath
    ) {
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

    public static FirstPartySignature restore(
            SignatureId signatureId,
            UserId ownerId,
            FileMetadata fileMetadata,
            String storagePath,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
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

    public void updateFile(
            FileMetadata fileMetadata,
            String storagePath
    ) {
        validateStoragePath(storagePath);
        this.fileMetadata = fileMetadata;
        this.storagePath = storagePath;
        updateTimestamp();
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
