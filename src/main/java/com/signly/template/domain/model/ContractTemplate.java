package com.signly.template.domain.model;

import com.signly.common.domain.AggregateRoot;
import com.signly.common.exception.ValidationException;
import com.signly.user.domain.model.UserId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
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

    private ContractTemplate(
            TemplateId templateId,
            UserId ownerId,
            String title,
            TemplateContent content,
            int version,
            TemplateStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        super(createdAt, updatedAt);
        this.templateId = templateId;
        this.ownerId = ownerId;
        this.title = title;
        this.content = content;
        this.version = version;
        this.status = status;
    }

    public static ContractTemplate create(
            UserId ownerId,
            String title,
            TemplateContent content
    ) {
        validateCreateParameters(ownerId, title, content);

        TemplateId templateId = TemplateId.generate();
        int initialVersion = 1;
        TemplateStatus initialStatus = TemplateStatus.ACTIVE;

        return new ContractTemplate(
                templateId,
                ownerId,
                title,
                content,
                initialVersion,
                initialStatus,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static ContractTemplate restore(
            TemplateId templateId,
            UserId ownerId,
            String title,
            TemplateContent content,
            int version,
            TemplateStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new ContractTemplate(
                templateId,
                ownerId,
                title,
                content,
                version,
                status,
                createdAt,
                updatedAt
        );
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
        if (this.status == TemplateStatus.ACTIVE) {
            throw new ValidationException("이미 활성화된 템플릿입니다");
        }

        if (this.status != TemplateStatus.DRAFT && this.status != TemplateStatus.ARCHIVED) {
            throw new ValidationException("활성화할 수 없는 상태입니다");
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
        return this.status == TemplateStatus.ARCHIVED;
    }

    public void updateTitle(String newTitle) {
        if (this.status == TemplateStatus.ACTIVE) {
            throw new ValidationException("활성화된 템플릿의 제목은 수정할 수 없습니다");
        }

        validateTitle(newTitle);
        this.title = newTitle;
        updateTimestamp();
    }

    public void addSection(TemplateSection section) {
        if (this.status == TemplateStatus.ACTIVE) {
            throw new ValidationException("활성화된 템플릿은 수정할 수 없습니다");
        }

        if (section == null) {
            throw new ValidationException("섹션은 필수입니다");
        }

        var sections = new java.util.ArrayList<>(this.content.sections());
        sections.add(section);

        this.content = TemplateContent.of(
                this.content.metadata(),
                sections
        );
        this.version++;
        updateTimestamp();
    }

    public void updateSection(
            String sectionId,
            String newContent
    ) {
        if (this.status == TemplateStatus.ACTIVE) {
            throw new ValidationException("활성화된 템플릿은 수정할 수 없습니다");
        }

        var sections = new java.util.ArrayList<>(this.content.sections());
        boolean found = false;

        for (TemplateSection section : sections) {
            if (section.getSectionId().equals(sectionId)) {
                section.setContent(newContent);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new ValidationException("섹션을 찾을 수 없습니다: " + sectionId);
        }

        this.content = TemplateContent.of(
                this.content.metadata(),
                sections
        );
        this.version++;
        updateTimestamp();
    }

    public void reorderSections(java.util.List<String> sectionIds) {
        if (this.status == TemplateStatus.ACTIVE) {
            throw new ValidationException("활성화된 템플릿은 수정할 수 없습니다");
        }

        var sections = new java.util.ArrayList<>(this.content.sections());

        if (sectionIds.size() != sections.size()) {
            throw new ValidationException("섹션 ID 개수가 일치하지 않습니다");
        }

        var reorderedSections = new java.util.ArrayList<TemplateSection>();
        int order = 0;

        for (String sectionId : sectionIds) {
            TemplateSection found = sections.stream()
                    .filter(s -> s.getSectionId().equals(sectionId))
                    .findFirst()
                    .orElseThrow(() -> new ValidationException("섹션을 찾을 수 없습니다: " + sectionId));

            found.setOrder(order++);
            reorderedSections.add(found);
        }

        this.content = TemplateContent.of(
                this.content.metadata(),
                reorderedSections
        );
        this.version++;
        updateTimestamp();
    }

    public void removeSection(String sectionId) {
        if (this.status == TemplateStatus.ACTIVE) {
            throw new ValidationException("활성화된 템플릿은 수정할 수 없습니다");
        }

        var sections = new java.util.ArrayList<>(this.content.sections());
        boolean removed = sections.removeIf(s -> s.getSectionId().equals(sectionId));

        if (!removed) {
            throw new ValidationException("섹션을 찾을 수 없습니다: " + sectionId);
        }

        if (sections.isEmpty()) {
            throw new ValidationException("최소 한 개 이상의 섹션이 필요합니다");
        }

        int order = 0;
        for (TemplateSection section : sections) {
            section.setOrder(order++);
        }

        this.content = TemplateContent.of(
                this.content.metadata(),
                sections
        );
        this.version++;
        updateTimestamp();
    }

    private static void validateCreateParameters(
            UserId ownerId,
            String title,
            TemplateContent content
    ) {
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
