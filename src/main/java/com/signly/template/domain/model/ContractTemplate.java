package com.signly.template.domain.model;

import com.signly.common.domain.AggregateRoot;
import com.signly.common.exception.ValidationException;
import com.signly.user.domain.model.UserId;

import java.time.LocalDateTime;

public class ContractTemplate extends AggregateRoot {

    private TemplateId templateId;
    private UserId ownerId;
    private String title;
    private TemplateContent content;
    private int version;
    private TemplateStatus status;

    protected ContractTemplate() {
        super();
    }

    private ContractTemplate(TemplateId templateId, UserId ownerId, String title,
                           TemplateContent content, int version, TemplateStatus status,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(createdAt, updatedAt);
        this.templateId = templateId;
        this.ownerId = ownerId;
        this.title = title;
        this.content = content;
        this.version = version;
        this.status = status;
    }

    public static ContractTemplate create(UserId ownerId, String title, TemplateContent content) {
        validateCreateParameters(ownerId, title, content);

        TemplateId templateId = TemplateId.generate();
        int initialVersion = 1;
        TemplateStatus initialStatus = TemplateStatus.DRAFT;

        return new ContractTemplate(templateId, ownerId, title, content, initialVersion, initialStatus,
                                  LocalDateTime.now(), LocalDateTime.now());
    }

    public static ContractTemplate restore(TemplateId templateId, UserId ownerId, String title,
                                         TemplateContent content, int version, TemplateStatus status,
                                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new ContractTemplate(templateId, ownerId, title, content, version, status,
                                   createdAt, updatedAt);
    }

    public void updateContent(TemplateContent newContent) {
        if (this.status == TemplateStatus.ACTIVE) {
            throw new ValidationException("활성화된 템플릿은 수정할 수 없습니다");
        }

        validateContent(newContent);
        this.content = newContent;
        this.version++;
        updateTimestamp();
    }

    public void activate() {
        if (this.status != TemplateStatus.DRAFT) {
            throw new ValidationException("DRAFT 상태에서만 활성화할 수 있습니다");
        }

        this.status = TemplateStatus.ACTIVE;
        updateTimestamp();
    }

    public void archive() {
        if (this.status == TemplateStatus.ARCHIVED) {
            throw new ValidationException("이미 보관된 템플릿입니다");
        }

        this.status = TemplateStatus.ARCHIVED;
        updateTimestamp();
    }

    public boolean canDelete() {
        return this.status == TemplateStatus.DRAFT;
    }

    public void updateTitle(String newTitle) {
        if (this.status == TemplateStatus.ACTIVE) {
            throw new ValidationException("활성화된 템플릿의 제목은 수정할 수 없습니다");
        }

        validateTitle(newTitle);
        this.title = newTitle;
        updateTimestamp();
    }

    private static void validateCreateParameters(UserId ownerId, String title, TemplateContent content) {
        if (ownerId == null) {
            throw new ValidationException("소유자 ID는 필수입니다");
        }
        validateTitle(title);
        validateContent(content);
    }

    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("템플릿 제목은 필수입니다");
        }
        if (title.trim().length() > 255) {
            throw new ValidationException("템플릿 제목은 255자를 초과할 수 없습니다");
        }
    }

    private static void validateContent(TemplateContent content) {
        if (content == null) {
            throw new ValidationException("템플릿 내용은 필수입니다");
        }
    }

    public TemplateId getTemplateId() {
        return templateId;
    }

    public UserId getOwnerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public TemplateContent getContent() {
        return content;
    }

    public int getVersion() {
        return version;
    }

    public TemplateStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return this.status == TemplateStatus.ACTIVE;
    }

    public boolean isDraft() {
        return this.status == TemplateStatus.DRAFT;
    }

    public boolean isArchived() {
        return this.status == TemplateStatus.ARCHIVED;
    }
}