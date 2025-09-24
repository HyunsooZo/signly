package com.signly.document.infrastructure.persistence.mapper;

import com.signly.contract.domain.model.ContractId;
import com.signly.document.domain.model.Document;
import com.signly.document.domain.model.DocumentId;
import com.signly.document.domain.model.FileMetadata;
import com.signly.document.infrastructure.persistence.entity.DocumentJpaEntity;
import com.signly.user.domain.model.UserId;
import org.springframework.stereotype.Component;

@Component
public class DocumentJpaMapper {

    public DocumentJpaEntity toEntity(Document document) {
        return new DocumentJpaEntity(
            document.getId().getValue(),
            document.getContractId().getValue(),
            document.getUploadedBy().getValue(),
            document.getType(),
            document.getMetadata().getFileName(),
            document.getMetadata().getFileName(),
            document.getMetadata().getMimeType(),
            document.getMetadata().getFileSize(),
            document.getMetadata().getChecksum(),
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