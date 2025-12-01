package com.deally.document.infrastructure.persistence.mapper;

import com.deally.contract.domain.model.ContractId;
import com.deally.document.domain.model.Document;
import com.deally.document.domain.model.DocumentId;
import com.deally.document.domain.model.FileMetadata;
import com.deally.document.infrastructure.persistence.entity.DocumentJpaEntity;
import com.deally.user.domain.model.UserId;
import org.springframework.stereotype.Component;

@Component
public class DocumentJpaMapper {

    public DocumentJpaEntity toEntity(Document document) {
        return new DocumentJpaEntity(
                document.getId().value(),
                document.getContractId().value(),
                document.getUploadedBy().value(),
                document.getType(),
                document.getMetadata().fileName(),
                document.getMetadata().fileName(),
                document.getMetadata().mimeType(),
                document.getMetadata().fileSize(),
                document.getMetadata().checksum(),
                document.getStoragePath()
        );
    }

    public Document toDomain(DocumentJpaEntity entity) {
        FileMetadata metadata = FileMetadata.create(
                entity.getOriginalFilename(),
                entity.getContentType(),
                entity.getFileSize(),
                entity.getChecksum()
        );

        return Document.restore(
                DocumentId.of(entity.getId()),
                ContractId.of(entity.getContractId()),
                UserId.of(entity.getUploadedBy()),
                entity.getType(),
                metadata,
                entity.getStoragePath(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}