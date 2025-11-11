package com.signly.document.application;

import com.signly.common.exception.ForbiddenException;
import com.signly.common.exception.NotFoundException;
import com.signly.common.exception.ValidationException;
import com.signly.contract.domain.model.Contract;
import com.signly.contract.domain.model.ContractId;
import com.signly.contract.domain.repository.ContractRepository;
import com.signly.contract.domain.model.GeneratedPdf;
import com.signly.document.application.dto.CreateDocumentCommand;
import com.signly.document.application.dto.DocumentResponse;
import com.signly.document.application.mapper.DocumentDtoMapper;
import com.signly.document.domain.model.Document;
import com.signly.document.domain.model.DocumentId;
import com.signly.document.domain.model.DocumentType;
import com.signly.document.domain.model.FileMetadata;
import com.signly.document.domain.repository.DocumentRepository;
import com.signly.document.infrastructure.storage.FileStorageService;
import com.signly.user.domain.model.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ContractRepository contractRepository;
    private final FileStorageService fileStorageService;
    private final DocumentDtoMapper documentDtoMapper;

    private static final String PDF_CHECKSUM_ALGORITHM = "SHA-256";

    public DocumentService(DocumentRepository documentRepository,
                         ContractRepository contractRepository,
                         FileStorageService fileStorageService,
                         DocumentDtoMapper documentDtoMapper) {
        this.documentRepository = documentRepository;
        this.contractRepository = contractRepository;
        this.fileStorageService = fileStorageService;
        this.documentDtoMapper = documentDtoMapper;
    }

    public DocumentResponse uploadDocument(String userId, CreateDocumentCommand command) {
        UserId userIdObj = UserId.of(userId);
        ContractId contractId = ContractId.of(command.contractId());

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateUploadAccess(userIdObj, contract, command.type());

        FileMetadata metadata = FileMetadata.create(
            command.fileName(),
            command.mimeType(),
            command.fileSize(),
            command.checksum()
        );

        String storagePath = fileStorageService.storeFile(command.fileData(), metadata);

        Document document = Document.create(contractId, userIdObj, command.type(), metadata, storagePath);
        Document savedDocument = documentRepository.save(document);

        return documentDtoMapper.toResponse(savedDocument);
    }

    public Document storeContractPdf(Contract contract, GeneratedPdf pdf) {
        if (contract == null) {
            throw new ValidationException("계약서 정보가 필요합니다");
        }
        if (pdf == null) {
            throw new ValidationException("PDF 데이터가 필요합니다");
        }

        ContractId contractId = contract.getId();
        UserId uploaderId = contract.getCreatorId();

        removeExistingContractPdf(contractId);

        String checksum = calculateChecksum(pdf.content());
        FileMetadata metadata = FileMetadata.create(
                ensurePdfExtension(pdf.fileName()),
                pdf.getContentType(),
                pdf.sizeInBytes(),
                checksum
        );

        String storagePath = fileStorageService.storeFile(pdf.content(), metadata);
        Document document = Document.create(contractId, uploaderId, DocumentType.CONTRACT_PDF, metadata, storagePath);
        return documentRepository.save(document);
    }

    private void removeExistingContractPdf(ContractId contractId) {
        List<Document> existing = documentRepository.findByContractIdAndType(contractId, DocumentType.CONTRACT_PDF);
        for (Document document : existing) {
            fileStorageService.deleteFile(document.getStoragePath());
            documentRepository.delete(document);
        }
    }

    private String ensurePdfExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "contract.pdf";
        }
        return fileName.toLowerCase().endsWith(".pdf") ? fileName : fileName + ".pdf";
    }

    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(PDF_CHECKSUM_ALGORITHM);
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new ValidationException("체크섬 계산을 위한 알고리즘이 없습니다");
        }
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(String userId, String documentId) {
        UserId userIdObj = UserId.of(userId);
        DocumentId docId = DocumentId.of(documentId);

        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다"));

        Contract contract = contractRepository.findById(document.getContractId())
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateAccessPermission(userIdObj, contract);

        return documentDtoMapper.toResponse(document);
    }

    @Transactional(readOnly = true)
    public byte[] downloadDocument(String userId, String documentId) {
        UserId userIdObj = UserId.of(userId);
        DocumentId docId = DocumentId.of(documentId);

        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다"));

        Contract contract = contractRepository.findById(document.getContractId())
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateAccessPermission(userIdObj, contract);

        return fileStorageService.loadFile(document.getStoragePath());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByContract(String userId, String contractId) {
        UserId userIdObj = UserId.of(userId);
        ContractId contractIdObj = ContractId.of(contractId);

        Contract contract = contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateAccessPermission(userIdObj, contract);

        List<Document> documents = documentRepository.findByContractId(contractIdObj);
        return documents.stream()
                .map(documentDtoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByContractAndType(String userId, String contractId, DocumentType type) {
        UserId userIdObj = UserId.of(userId);
        ContractId contractIdObj = ContractId.of(contractId);

        Contract contract = contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateAccessPermission(userIdObj, contract);

        List<Document> documents = documentRepository.findByContractIdAndType(contractIdObj, type);
        return documents.stream()
                .map(documentDtoMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteDocument(String userId, String documentId) {
        UserId userIdObj = UserId.of(userId);
        DocumentId docId = DocumentId.of(documentId);

        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다"));

        Contract contract = contractRepository.findById(document.getContractId())
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateDeletePermission(userIdObj, contract, document);

        fileStorageService.deleteFile(document.getStoragePath());
        documentRepository.delete(document);
    }

    private void validateUploadAccess(UserId userId, Contract contract, DocumentType type) {
        if (!contract.getCreatorId().equals(userId)) {
            throw new ForbiddenException("문서를 업로드할 권한이 없습니다");
        }

        if (type == DocumentType.CONTRACT_PDF) {
            if (documentRepository.existsByContractIdAndType(contract.getId(), DocumentType.CONTRACT_PDF)) {
                throw new ValidationException("계약서 PDF는 하나만 업로드할 수 있습니다");
            }
        }
    }

    private void validateAccessPermission(UserId userId, Contract contract) {
        if (!contract.getCreatorId().equals(userId) &&
            !contract.getFirstParty().email().equals(userId.value()) &&
            !contract.getSecondParty().email().equals(userId.value())) {
            throw new ForbiddenException("해당 문서에 접근할 권한이 없습니다");
        }
    }

    private void validateDeletePermission(UserId userId, Contract contract, Document document) {
        if (!contract.getCreatorId().equals(userId) && !document.getUploadedBy().equals(userId)) {
            throw new ForbiddenException("해당 문서를 삭제할 권한이 없습니다");
        }

        if (document.getType() == DocumentType.CONTRACT_PDF &&
            !contract.getStatus().name().equals("DRAFT")) {
            throw new ValidationException("초안 상태의 계약서만 PDF를 삭제할 수 있습니다");
        }
    }
}
