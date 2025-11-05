package com.signly.document.infrastructure.persistence;

import com.signly.contract.domain.model.ContractId;
import com.signly.document.domain.model.Document;
import com.signly.document.domain.model.DocumentId;
import com.signly.document.domain.model.DocumentType;
import com.signly.document.domain.repository.DocumentRepository;
import com.signly.document.infrastructure.persistence.entity.DocumentJpaEntity;
import com.signly.document.infrastructure.persistence.mapper.DocumentJpaMapper;
import com.signly.document.infrastructure.persistence.repository.DocumentJpaRepository;
import com.signly.user.domain.model.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DocumentRepositoryImpl implements DocumentRepository {

    private final DocumentJpaRepository jpaRepository;
    private final DocumentJpaMapper mapper;

    public DocumentRepositoryImpl(DocumentJpaRepository jpaRepository, DocumentJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Document save(Document document) {
        DocumentJpaEntity entity = mapper.toEntity(document);
        DocumentJpaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Document> findById(DocumentId id) {
        return jpaRepository.findById(id.getValue())
            .map(mapper::toDomain);
    }

    @Override
    public List<Document> findByContractId(ContractId contractId) {
        return jpaRepository.findByContractIdOrderByCreatedAtDesc(contractId.getValue())
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Document> findByUploadedBy(UserId uploadedBy) {
        return jpaRepository.findByUploadedByOrderByCreatedAtDesc(uploadedBy.getValue())
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public void delete(Document document) {
        jpaRepository.deleteById(document.getId().getValue());
    }

    @Override
    public List<Document> findByContractIdAndType(ContractId contractId, DocumentType type) {
        return jpaRepository.findByContractIdAndTypeOrderByCreatedAtDesc(
                    contractId.getValue(), 
                    type.name()
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByContractIdAndType(ContractId contractId, DocumentType type) {
        return jpaRepository.existsByContractIdAndType(
                    contractId.getValue(), 
                    type.name()
                );
    }

    @Override
    public long countByContractId(ContractId contractId) {
        return jpaRepository.countByContractId(contractId.getValue());
    }
}