package com.signly.signature.application;

import com.signly.common.audit.aop.Auditable;
import com.signly.common.audit.domain.model.AuditAction;
import com.signly.common.audit.domain.model.EntityType;
import com.signly.common.exception.ValidationException;
import com.signly.common.storage.FileStorageService;
import com.signly.document.domain.model.FileMetadata;
import com.signly.signature.application.dto.FirstPartySignatureResponse;
import com.signly.signature.domain.model.FirstPartySignature;
import com.signly.signature.domain.repository.FirstPartySignatureRepository;
import com.signly.user.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
@Transactional
@RequiredArgsConstructor
public class FirstPartySignatureService {

    private static final String STORAGE_CATEGORY_PREFIX = "signatures/first-party";

    private final FirstPartySignatureRepository signatureRepository;
    private final FileStorageService fileStorageService;

    @Auditable(
            entityType = EntityType.FIRST_PARTY_SIGNATURE,
            action = AuditAction.FIRST_PARTY_SIGNATURE_UPLOADED,
            entityIdParam = "#ownerId"
    )
    public void uploadSignature(
            String ownerId,
            String dataUrl
    ) {
        var payload = parseDataUrl(dataUrl);

        String fileName = "signature-" + ownerId + "." + payload.extension();
        var storedFile = fileStorageService.storeFile(
                payload.data(),
                fileName,
                payload.contentType(),
                buildCategory(ownerId)
        );

        var metadata = FileMetadata.create(
                storedFile.originalFilename(),
                storedFile.contentType(),
                storedFile.size(),
                calculateChecksum(payload.data())
        );

        UserId userId = UserId.of(ownerId);

        var signature = signatureRepository.findByOwnerId(userId)
                .map(existing -> updateExistingSignature(existing, metadata, storedFile.filePath()))
                .orElseGet(() -> createNewSignature(userId, metadata, storedFile.filePath()));

        FirstPartySignatureResponse.from(signature);
    }

    @Transactional(readOnly = true)
    public FirstPartySignatureResponse getSignature(String ownerId) {
        var signature = signatureRepository.findByOwnerId(UserId.of(ownerId))
                .orElseThrow(() -> new ValidationException("등록된 갑 서명을 찾을 수 없습니다."));
        return FirstPartySignatureResponse.from(signature);
    }

    @Transactional(readOnly = true)
    public boolean hasSignature(String ownerId) {
        return signatureRepository.existsByOwnerId(UserId.of(ownerId));
    }

    @Transactional(readOnly = true)
    public String getSignatureDataUrl(String ownerId) {
        var signature = signatureRepository.findByOwnerId(UserId.of(ownerId))
                .orElseThrow(() -> new ValidationException("등록된 갑 서명을 찾을 수 없습니다."));

        byte[] fileBytes = fileStorageService.loadFile(signature.getStoragePath());
        String base64 = Base64.getEncoder().encodeToString(fileBytes);
        return "data:" + signature.getFileMetadata().mimeType() + ";base64," + base64;
    }

    @Transactional(readOnly = true)
    public void ensureSignatureExists(String ownerId) {
        if (!hasSignature(ownerId)) {
            throw new ValidationException("계약을 진행하기 전에 갑 서명을 먼저 등록해야 합니다.");
        }
    }

    private FirstPartySignature updateExistingSignature(
            FirstPartySignature existing,
            FileMetadata metadata,
            String storagePath
    ) {
        String previousPath = existing.getStoragePath();
        existing.updateFile(metadata, storagePath);
        var saved = signatureRepository.save(existing);
        if (!previousPath.equals(storagePath)) {
            fileStorageService.deleteFile(previousPath);
        }
        return saved;
    }

    private FirstPartySignature createNewSignature(
            UserId ownerId,
            FileMetadata metadata,
            String storagePath
    ) {
        FirstPartySignature newSignature = FirstPartySignature.create(ownerId, metadata, storagePath);
        return signatureRepository.save(newSignature);
    }

    private String buildCategory(String ownerId) {
        return STORAGE_CATEGORY_PREFIX + "/" + ownerId;
    }

    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("체크섬 계산을 위한 알고리즘을 찾을 수 없습니다", e);
        }
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

    private record ImagePayload(byte[] data, String contentType, String extension) {}
}
