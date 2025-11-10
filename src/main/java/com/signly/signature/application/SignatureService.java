package com.signly.signature.application;

import com.signly.common.exception.NotFoundException;
import com.signly.common.exception.ValidationException;
import com.signly.common.storage.FileStorageService;
import com.signly.common.storage.StoredFile;
import com.signly.contract.domain.model.ContractId;
import com.signly.contract.application.dto.CreateSignatureCommand;
import com.signly.contract.application.dto.SignatureResponse;
import com.signly.contract.application.mapper.SignatureDtoMapper;
import com.signly.contract.domain.model.Signature;
import com.signly.contract.domain.repository.SignatureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SignatureService {

    private static final Logger logger = LoggerFactory.getLogger(SignatureService.class);

    private static final String STORAGE_CATEGORY_PREFIX = "signatures/contracts";
    private final SignatureRepository signatureRepository;
    private final SignatureDtoMapper mapper;
    private final FileStorageService fileStorageService;

    public SignatureService(SignatureRepository signatureRepository,
                            SignatureDtoMapper mapper,
                            FileStorageService fileStorageService) {
        this.signatureRepository = signatureRepository;
        this.mapper = mapper;
        this.fileStorageService = fileStorageService;
    }

    public SignatureResponse createSignature(CreateSignatureCommand command) {
        String normalizedEmail = normalizeEmail(command.signerEmail());
        logger.info("서명 생성 시작: contractId={}, signerEmail={}", command.contractId(), normalizedEmail);

        ContractId contractId = ContractId.of(command.contractId());

        // 이미 서명이 존재하는 경우 기존 서명 반환 (중복 방지)
        if (signatureRepository.existsByContractIdAndSignerEmail(contractId, normalizedEmail)) {
            logger.warn("이미 서명이 존재함, 기존 서명 반환: contractId={}, signerEmail={}",
                command.contractId(), normalizedEmail);
            Signature existingSignature = signatureRepository
                .findByContractIdAndSignerEmail(contractId, normalizedEmail)
                .orElseThrow(() -> new NotFoundException("서명을 찾을 수 없습니다"));
            return mapper.toResponse(existingSignature);
        }

        ImagePayload payload = parseDataUrl(command.signatureData());

        String category = buildCategory(contractId.value(), normalizedEmail);
        String originalFileName = buildFileName(contractId.value(), normalizedEmail, payload.extension());
        StoredFile storedFile = fileStorageService.storeFile(
                payload.data(),
                originalFileName,
                payload.contentType(),
                category
        );

        Signature signature = Signature.create(
                normalizedEmail,
                command.signerName(),
                command.signatureData(),
                command.ipAddress(),
                command.deviceInfo(),
                storedFile.filePath()
        );

        if (!signature.validate()) {
            throw new ValidationException("서명 데이터가 유효하지 않습니다");
        }

        signatureRepository.save(signature);

        logger.info("서명 생성 완료: signerEmail={}", normalizedEmail);
        return mapper.toResponse(signature);
    }

    private ImagePayload parseDataUrl(String dataUrl) {
        if (dataUrl == null || dataUrl.trim().isEmpty()) {
            throw new ValidationException("서명 데이터를 전달해주세요.");
        }

        if (!dataUrl.startsWith("data:")) {
            throw new ValidationException("올바른 데이터 URL 형식이 아닙니다.");
        }

        int commaIndex = dataUrl.indexOf(',');
        if (commaIndex <= 0) {
            throw new ValidationException("잘못된 데이터 URL입니다.");
        }

        String metadata = dataUrl.substring(5, commaIndex);
        String base64Data = dataUrl.substring(commaIndex + 1);

        if (!metadata.contains(";base64")) {
            throw new ValidationException("지원하지 않는 서명 데이터 형식입니다.");
        }

        String contentType = metadata.substring(0, metadata.indexOf(';'));
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ValidationException("서명은 이미지 형식이어야 합니다.");
        }

        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64Data.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("서명 데이터를 해석할 수 없습니다.");
        }

        if (decoded.length == 0) {
            throw new ValidationException("서명 데이터가 비어 있습니다.");
        }

        String extension = switch (contentType) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            case "image/gif" -> "gif";
            default -> throw new ValidationException("지원하지 않는 이미지 형식입니다.");
        };

        return new ImagePayload(decoded, contentType, extension);
    }

    private String buildCategory(String contractId, String signerEmail) {
        return STORAGE_CATEGORY_PREFIX + "/" + contractId + "/" + sanitizeEmail(signerEmail);
    }

    private String buildFileName(String contractId, String signerEmail, String extension) {
        return "signature-" + contractId + "-" + sanitizeEmail(signerEmail) + "." + extension;
    }

    private String sanitizeEmail(String email) {
        return email == null ? "unknown" : email.replaceAll("[^a-zA-Z0-9]+", "-");
    }

    private record ImagePayload(byte[] data, String contentType, String extension) {}

    @Transactional(readOnly = true)
    public SignatureResponse getSignature(String signatureId) {
        Signature signature = signatureRepository.findById(signatureId)
                .orElseThrow(() -> new NotFoundException("서명을 찾을 수 없습니다: " + signatureId));

        return mapper.toResponse(signature);
    }

    @Transactional(readOnly = true)
    public List<SignatureResponse> getSignaturesByContract(String contractId) {
        ContractId cId = ContractId.of(contractId);
        List<Signature> signatures = signatureRepository.findByContractId(cId);

        return signatures.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isContractSigned(String contractId, String signerEmail) {
        ContractId cId = ContractId.of(contractId);
        String normalizedEmail = normalizeEmail(signerEmail);
        boolean exists = signatureRepository.existsByContractIdAndSignerEmail(cId, normalizedEmail);
        logger.info("서명 여부 체크: contractId={}, signerEmail={}, exists={}", contractId, normalizedEmail, exists);
        return exists;
    }

    @Transactional(readOnly = true)
    public SignatureResponse getContractSignature(String contractId, String signerEmail) {
        ContractId cId = ContractId.of(contractId);
        String normalizedEmail = normalizeEmail(signerEmail);
        Signature signature = signatureRepository.findByContractIdAndSignerEmail(cId, normalizedEmail)
                .orElseThrow(() -> new NotFoundException("서명을 찾을 수 없습니다"));

        return mapper.toResponse(signature);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public void deleteSignature(String signatureId) {
        if (!signatureRepository.findById(signatureId).isPresent()) {
            throw new NotFoundException("서명을 찾을 수 없습니다: " + signatureId);
        }

        signatureRepository.delete(signatureId);
        logger.info("서명 삭제 완료: signatureId={}", signatureId);
    }
}
