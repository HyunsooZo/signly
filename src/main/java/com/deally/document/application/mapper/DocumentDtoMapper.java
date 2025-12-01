package com.deally.document.application.mapper;

import com.deally.document.application.dto.DocumentResponse;
import com.deally.document.domain.model.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentDtoMapper {

    public DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getId().value(),
                document.getContractId().value(),
                document.getUploadedBy().value(),
                document.getType(),
                document.getMetadata().fileName(),
                document.getMetadata().mimeType(),
                document.getMetadata().fileSize(),
                document.getStoragePath(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}