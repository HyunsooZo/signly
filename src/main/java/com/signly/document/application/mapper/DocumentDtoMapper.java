package com.signly.document.application.mapper;

import com.signly.document.application.dto.DocumentResponse;
import com.signly.document.domain.model.Document;
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