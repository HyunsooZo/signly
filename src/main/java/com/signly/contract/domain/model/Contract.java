package com.signly.contract.domain.model;

import com.signly.common.domain.AggregateRoot;
import com.signly.common.exception.ValidationException;
import com.signly.template.domain.model.TemplateId;
import com.signly.user.domain.model.UserId;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final SignToken signToken;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Contract(ContractId id, UserId creatorId, TemplateId templateId,
                    String title, ContractContent content, PartyInfo firstParty,
                    PartyInfo secondParty, LocalDateTime expiresAt) {
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
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Contract create(UserId creatorId, TemplateId templateId, String title,
                                ContractContent content, PartyInfo firstParty,
                                PartyInfo secondParty, LocalDateTime expiresAt) {
        validateTitle(title);
        validateExpirationDate(expiresAt);
        validateParties(firstParty, secondParty);

        return new Contract(ContractId.generate(), creatorId, templateId,
                          title.trim(), content, firstParty, secondParty, expiresAt);
    }

    public static Contract restore(ContractId id, UserId creatorId, TemplateId templateId,
                                 String title, ContractContent content, PartyInfo firstParty,
                                 PartyInfo secondParty, ContractStatus status,
                                 List<Signature> signatures, LocalDateTime expiresAt,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        Contract contract = new Contract(id, creatorId, templateId, title, content,
                                       firstParty, secondParty, expiresAt);
        contract.status = status;
        contract.signatures.addAll(signatures);
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

    private static void validateParties(PartyInfo firstParty, PartyInfo secondParty) {
        if (firstParty.getEmail().equals(secondParty.getEmail())) {
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

    public void sign(String signerEmail, String signerName, String signatureData, String ipAddress) {
        if (!status.canSign()) {
            throw new ValidationException("서명 대기 상태에서만 서명할 수 있습니다");
        }

        if (isExpired()) {
            expire();
            throw new ValidationException("만료된 계약서에는 서명할 수 없습니다");
        }

        if (!isValidSigner(signerEmail)) {
            throw new ValidationException("해당 계약서에 서명할 권한이 없습니다");
        }

        if (hasSignedBy(signerEmail)) {
            throw new ValidationException("이미 서명한 계약서입니다");
        }

        Signature signature = Signature.create(signerEmail, signerName, signatureData, ipAddress);
        this.signatures.add(signature);
        this.updatedAt = LocalDateTime.now();

        if (isFullySigned()) {
            this.status = ContractStatus.SIGNED;
        }
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

    private boolean isValidSigner(String email) {
        return firstParty.getEmail().equals(email.trim().toLowerCase()) ||
               secondParty.getEmail().equals(email.trim().toLowerCase());
    }

    private boolean hasSignedBy(String email) {
        return signatures.stream()
                .anyMatch(signature -> signature.isSignedBy(email));
    }

    private boolean isFullySigned() {
        return hasSignedBy(firstParty.getEmail()) && hasSignedBy(secondParty.getEmail());
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean canDelete() {
        return status == ContractStatus.DRAFT;
    }

    public List<String> getPendingSigners() {
        List<String> allSigners = Arrays.asList(firstParty.getEmail(), secondParty.getEmail());
        Set<String> signedEmails = signatures.stream()
                .map(Signature::getSignerEmail)
                .collect(Collectors.toSet());

        return allSigners.stream()
                .filter(email -> !signedEmails.contains(email))
                .collect(Collectors.toList());
    }

    public ContractId getId() {
        return id;
    }

    public UserId getCreatorId() {
        return creatorId;
    }

    public TemplateId getTemplateId() {
        return templateId;
    }

    public String getTitle() {
        return title;
    }

    public ContractContent getContent() {
        return content;
    }

    public PartyInfo getFirstParty() {
        return firstParty;
    }

    public PartyInfo getSecondParty() {
        return secondParty;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public List<Signature> getSignatures() {
        return Collections.unmodifiableList(signatures);
    }

    public SignToken getSignToken() {
        return signToken;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}