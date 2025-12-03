package com.signly.document.domain.repository;

import com.signly.contract.domain.model.ContractId;
import com.signly.document.domain.model.Document;
import com.signly.document.domain.model.DocumentId;
import com.signly.document.domain.model.DocumentType;
import com.signly.user.domain.model.UserId;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository {
    Document save(Document document);

    Optional<Document> findById(DocumentId documentId);

    void delete(Document document);

    List<Document> findByContractId(ContractId contractId);

    List<Document> findByContractIdAndType(
            ContractId contractId,
            DocumentType type
    );

    List<Document> findByUploadedBy(UserId userId);

    boolean existsByContractIdAndType(
            ContractId contractId,
            DocumentType type
    );

    long countByContractId(ContractId contractId);
}