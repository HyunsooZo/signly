package com.signly.contract.domain.model;

import com.signly.common.domain.AggregateRoot;
import com.signly.common.exception.ValidationException;
import com.signly.template.domain.model.TemplateId;
import com.signly.user.domain.model.UserId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Contract extends AggregateRoot {
    private final ContractId id;
    private final UserId creatorId;
    private final TemplateId templateId;
    private String title;
    private ContractContent content;
    private final PartyInfo firstParty;
    private final PartyInfo secondParty;
    private ContractStatus status;
    private final List<Signature> signatures;
    private SignToken signToken;
    private LocalDateTime expiresAt;
    private final PresetType presetType;
    private String pdfPath;  // 완료된 계약서 PDF 파일 경로
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Contract() {
        // for JPA and reflection
        this.id = null;
        this.creatorId = null;
        this.templateId = null;
        this.title = null;
        this.content = null;
        this.firstParty = null;
        this.secondParty = null;
        this.status = null;
        this.signatures = null;
        this.signToken = null;
        this.expiresAt = null;
        this.presetType = null;
        this.pdfPath = null;
        this.createdAt = null;
        this.updatedAt = null;
    }

    private Contract(
            ContractId id,
            UserId creatorId,
            TemplateId templateId,
            String title,
            ContractContent content,
            PartyInfo firstParty,
            PartyInfo secondParty,
            LocalDateTime expiresAt,
            PresetType presetType
    ) {
        this.id = id;
        this.creatorId = creatorId;
        this.templateId = templateId;
        this.title = title;
        this.content = content;
        this.firstParty = firstParty;
        this.secondParty = secondParty;
        this.status = ContractStatus.DRAFT;
        this.signatures = new ArrayList<>();
        this.signToken = SignToken.generate();
        this.expiresAt = expiresAt;
        this.presetType = presetType != null ? presetType : PresetType.NONE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Contract create(
            UserId creatorId,
            TemplateId templateId,
            String title,
            ContractContent content,
            PartyInfo firstParty,
            PartyInfo secondParty,
            LocalDateTime expiresAt
    ) {
        return create(creatorId, templateId, title, content, firstParty, secondParty, expiresAt, PresetType.NONE);
    }

    public static Contract create(
            UserId creatorId,
            TemplateId templateId,
            String title,
            ContractContent content,
            PartyInfo firstParty,
            PartyInfo secondParty,
            LocalDateTime expiresAt,
            PresetType presetType
    ) {
        validateTitle(title);
        validateExpirationDate(expiresAt);
        validateParties(firstParty, secondParty);

        return new Contract(ContractId.generate(), creatorId, templateId,
                title.trim(), content, firstParty, secondParty, expiresAt, presetType);
    }

    public static Contract restore(
            ContractId id,
            UserId creatorId,
            TemplateId templateId,
            String title,
            ContractContent content,
            PartyInfo firstParty,
            PartyInfo secondParty,
            ContractStatus status,
            List<Signature> signatures,
            SignToken signToken,
            LocalDateTime expiresAt,
            PresetType presetType,
            String pdfPath,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        Contract contract = new Contract(
                id,
                creatorId,
                templateId,
                title,
                content,
                firstParty,
                secondParty,
                expiresAt,
                presetType
        );
        contract.status = status;
        contract.signatures.addAll(signatures);
        contract.signToken = signToken;
        contract.pdfPath = pdfPath;
        contract.createdAt = createdAt;
        contract.updatedAt = updatedAt;
        return contract;
    }

    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("계약서 제목은 필수입니다");
        }
        if (title.length() > 200) {
            throw new ValidationException("계약서 제목은 200자를 초과할 수 없습니다");
        }
    }

    private static void validateExpirationDate(LocalDateTime expiresAt) {
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ValidationException("만료일은 현재 시간으로부터 최소 1시간 이후여야 합니다");
        }
    }

    private static void validateParties(
            PartyInfo firstParty,
            PartyInfo secondParty
    ) {
        if (firstParty.email().equals(secondParty.email())) {
            throw new ValidationException("당사자들의 이메일은 서로 달라야 합니다");
        }
    }

    public void updateTitle(String newTitle) {
        if (!status.canUpdate()) {
            throw new ValidationException("초안 상태에서만 제목을 수정할 수 있습니다");
        }
        validateTitle(newTitle);
        this.title = newTitle.trim();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateContent(ContractContent newContent) {
        if (!status.canUpdate()) {
            throw new ValidationException("초안 상태에서만 내용을 수정할 수 있습니다");
        }
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateExpirationDate(LocalDateTime newExpiresAt) {
        if (!status.canUpdate()) {
            throw new ValidationException("초안 상태에서만 만료일을 수정할 수 있습니다");
        }
        validateExpirationDate(newExpiresAt);
        this.expiresAt = newExpiresAt;
        this.updatedAt = LocalDateTime.now();
    }

    public void sendForSigning() {
        if (status != ContractStatus.DRAFT) {
            throw new ValidationException("초안 상태에서만 서명 요청을 보낼 수 있습니다");
        }
        this.status = ContractStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 서명 추가 (단순한 데이터 추가만 담당)
     * 복잡한 비즈니스 로직은 ContractSigningService에서 처리
     */
    public void addSignature(Signature signature) {
        validateCanAddSignature();
        this.signatures.add(signature);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 모든 서명이 완료된 상태로 변경
     */
    public void markAsFullySigned() {
        if (!status.canSign()) {
            throw new ValidationException("서명 대기 상태에서만 완료할 수 있습니다");
        }
        this.status = ContractStatus.SIGNED;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        if (!status.canComplete()) {
            throw new ValidationException("서명 완료 상태에서만 완료할 수 있습니다");
        }
        this.status = ContractStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (!status.canCancel()) {
            throw new ValidationException("초안 또는 서명 대기 상태에서만 취소할 수 있습니다");
        }
        this.status = ContractStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void expire() {
        if (status.isFinal()) {
            throw new ValidationException("이미 완료된 계약서는 만료시킬 수 없습니다");
        }
        this.status = ContractStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 서명 추가 가능 여부 검증
     */
    private void validateCanAddSignature() {
        if (!status.canSign()) {
            throw new ValidationException("서명 대기 상태에서만 서명할 수 있습니다");
        }
    }

    /**
     * 서명자 권한 확인
     */
    private boolean isValidSigner(String email) {
        return firstParty.email().equals(email.trim().toLowerCase()) ||
                secondParty.email().equals(email.trim().toLowerCase());
    }

    /**
     * 특정 이메일로 서명했는지 확인
     */
    private boolean hasSignedBy(String email) {
        return signatures.stream()
                .anyMatch(signature -> signature.isSignedBy(email));
    }

    /**
     * 모든 당사자가 서명했는지 확인
     */
    public boolean isFullySigned() {
        return hasSignedBy(firstParty.email()) && hasSignedBy(secondParty.email());
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean canDelete() {
        return status == ContractStatus.DRAFT;
    }

    public List<String> getPendingSigners() {
        List<String> allSigners = Arrays.asList(firstParty.email(), secondParty.email());
        Set<String> signedEmails = signatures.stream()
                .map(Signature::signerEmail)
                .collect(Collectors.toSet());

        return allSigners.stream()
                .filter(email -> !signedEmails.contains(email))
                .collect(Collectors.toList());
    }

    public List<Signature> getSignatures() {
        return Collections.unmodifiableList(signatures);
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasPdfPath() {
        return pdfPath != null && !pdfPath.isEmpty();
    }
}
