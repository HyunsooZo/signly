package com.signly.document.application.mapper;

import com.signly.document.application.dto.DocumentResponse;
import com.signly.document.domain.model.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentDtoMapper {

    public DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
            document.getId().getValue(),
            document.getContractId().getValue(),
            document.getUploadedBy().getValue(),
            document.getType(),
            document.getMetadata().getFileName(),
            document.getMetadata().getMimeType(),
            document.getMetadata().getFileSize(),
            document.getStoragePath(),
            document.getCreatedAt(),
            document.getUpdatedAt()
        );
    }
}