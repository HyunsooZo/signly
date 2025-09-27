package com.signly.signature.infrastructure.persistence.mapper;

import com.signly.document.domain.model.FileMetadata;
import com.signly.signature.domain.model.FirstPartySignature;
import com.signly.signature.domain.model.SignatureId;
import com.signly.signature.infrastructure.persistence.entity.FirstPartySignatureEntity;
import com.signly.user.domain.model.UserId;
import org.springframework.stereotype.Component;

@Component
public class FirstPartySignatureEntityMapper {

    public FirstPartySignatureEntity toEntity(FirstPartySignature signature) {
        FileMetadata metadata = signature.getFileMetadata();
        return new FirstPartySignatureEntity(
                signature.getSignatureId().value(),
                signature.getOwnerId().getValue(),
                signature.getStoragePath(),
                metadata.getFileName(),
                metadata.getMimeType(),
                metadata.getFileSize(),
                metadata.getChecksum(),
                signature.getCreatedAt(),
                signature.getUpdatedAt()
        );
    }

    public FirstPartySignature toDomain(FirstPartySignatureEntity entity) {
        return FirstPartySignature.restore(
                SignatureId.of(entity.getSignatureId()),
                UserId.of(entity.getOwnerId()),
                FileMetadata.create(
                        entity.getOriginalFilename(),
                        entity.getMimeType(),
                        entity.getFileSize(),
                        entity.getChecksum()
                ),
                entity.getStoragePath(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
