package com.signly.template.application;

import com.signly.common.audit.aop.Auditable;
import com.signly.common.audit.domain.model.AuditAction;
import com.signly.common.audit.domain.model.EntityType;
import com.signly.common.cache.CacheEvictionService;
import com.signly.common.exception.ForbiddenException;
import com.signly.common.exception.NotFoundException;
import com.signly.common.exception.ValidationException;
import com.signly.template.application.dto.CreateTemplateCommand;
import com.signly.template.application.dto.TemplateResponse;
import com.signly.template.application.dto.UpdateTemplateCommand;
import com.signly.template.application.mapper.TemplateDtoMapper;
import com.signly.template.domain.model.ContractTemplate;
import com.signly.template.domain.model.TemplateContent;
import com.signly.template.domain.model.TemplateId;
import com.signly.template.domain.model.TemplateStatus;
import com.signly.template.domain.repository.TemplateRepository;
import com.signly.user.domain.model.UserId;
import com.signly.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final TemplateDtoMapper templateDtoMapper;
    private final CacheEvictionService cacheEvictionService;

    @Auditable(
            action = AuditAction.TEMPLATE_CREATED,
            entityType = EntityType.TEMPLATE,
            entityIdParam = "#result.id"
    )
    public TemplateResponse createTemplate(
            String userId,
            CreateTemplateCommand command
    ) {
        var userIdObj = UserId.of(userId);
        var user = userRepository.findById(userIdObj)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        if (!user.canCreateTemplate()) {
            throw new ForbiddenException("템플릿을 생성할 권한이 없습니다");
        }

        if (templateRepository.existsByOwnerIdAndTitle(userIdObj, command.title())) {
            throw new ValidationException("이미 같은 제목의 템플릿이 존재합니다");
        }

        var content = TemplateContent.fromJson(command.sectionsJson());
        var template = ContractTemplate.create(userIdObj, command.title(), content);

        var savedTemplate = templateRepository.save(template);
        return templateDtoMapper.toResponse(savedTemplate);
    }

    @Auditable(
            action = AuditAction.TEMPLATE_UPDATED,
            entityType = EntityType.TEMPLATE,
            entityIdParam = "#templateId"
    )
    @CacheEvict(value = "templates", key = "#templateId")
    public TemplateResponse updateTemplate(
            String userId,
            String templateId,
            UpdateTemplateCommand command
    ) {
        var templateIdObj = TemplateId.of(templateId);
        var template = templateRepository.findById(templateIdObj)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다"));

        validateOwnership(userId, template);

        if (!command.title().equals(template.getTitle()) &&
                templateRepository.existsByOwnerIdAndTitle(template.getOwnerId(), command.title())) {
            throw new ValidationException("이미 같은 제목의 템플릿이 존재합니다");
        }

        template.updateTitle(command.title());
        var newContent = TemplateContent.fromJson(command.sectionsJson());
        template.updateContent(newContent);

        var updatedTemplate = templateRepository.save(template);
        log.info("Updated template: {} (cache evicted)", templateId);

        // 대시보드 통계 캐시 무효화
        cacheEvictionService.evictTemplateStats(template.getOwnerId().value());

        return templateDtoMapper.toResponse(updatedTemplate);
    }

    @Auditable(
            action = AuditAction.TEMPLATE_ACTIVATED,
            entityType = EntityType.TEMPLATE,
            entityIdParam = "#templateId"
    )
    @CacheEvict(value = "templates", key = "#templateId")
    public void activateTemplate(
            String userId,
            String templateId
    ) {
        var templateIdObj = TemplateId.of(templateId);
        var template = templateRepository.findById(templateIdObj)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다"));

        validateOwnership(userId, template);
        template.activate();
        templateRepository.save(template);
        log.info("Activated template: {} (cache evicted)", templateId);

        // 대시보드 통계 캐시 무효화
        cacheEvictionService.evictTemplateStats(template.getOwnerId().value());
    }

    @Auditable(
            action = AuditAction.TEMPLATE_ARCHIVED,
            entityType = EntityType.TEMPLATE,
            entityIdParam = "#templateId"
    )
    @CacheEvict(value = "templates", key = "#templateId")
    public void archiveTemplate(
            String userId,
            String templateId
    ) {
        var templateIdObj = TemplateId.of(templateId);
        var template = templateRepository.findById(templateIdObj)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다"));

        validateOwnership(userId, template);
        template.archive();
        templateRepository.save(template);
        log.info("Archived template: {} (cache evicted)", templateId);

        // 대시보드 통계 캐시 무효화
        cacheEvictionService.evictTemplateStats(template.getOwnerId().value());
    }

    @Auditable(
            action = AuditAction.TEMPLATE_DELETED,
            entityType = EntityType.TEMPLATE,
            entityIdParam = "#templateId"
    )
    @CacheEvict(value = "templates", key = "#templateId")
    public void deleteTemplate(
            String userId,
            String templateId
    ) {
        var templateIdObj = TemplateId.of(templateId);
        var template = templateRepository.findById(templateIdObj)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다"));

        validateOwnership(userId, template);

        if (!template.canDelete()) {
            throw new ValidationException("보류된 템플릿만 삭제할 수 있습니다");
        }

        templateRepository.delete(template);
        log.info("Deleted template: {} (cache evicted)", templateId);
    }

    /**
     * 템플릿 조회 (캐싱 적용)
     * 캐시 키: templateId (사용자별로 다른 템플릿이므로 userId는 키에 불필요)
     * TTL: 1시간
     */
    @Cacheable(value = "templates", key = "#templateId")
    @Transactional(readOnly = true)
    public TemplateResponse getTemplate(
            String userId,
            String templateId
    ) {
        var templateIdObj = TemplateId.of(templateId);
        var template = templateRepository.findById(templateIdObj)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다"));

        validateOwnership(userId, template);
        log.info("Loaded template from DB: {} (cache miss)", templateId);
        return templateDtoMapper.toResponse(template);
    }

    @Transactional(readOnly = true)
    public Page<TemplateResponse> getTemplatesByOwner(
            String userId,
            Pageable pageable
    ) {
        var userIdObj = UserId.of(userId);
        var templates = templateRepository.findByOwnerId(userIdObj, pageable);
        return templates.map(templateDtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TemplateResponse> getTemplatesByOwnerAndStatus(
            String userId,
            TemplateStatus status,
            Pageable pageable
    ) {
        var userIdObj = UserId.of(userId);
        var templates = templateRepository.findByOwnerIdAndStatus(userIdObj, status, pageable);
        return templates.map(templateDtoMapper::toResponse);
    }

    private void validateOwnership(
            String userId,
            ContractTemplate template
    ) {
        if (!template.getOwnerId().value().equals(userId)) {
            throw new ForbiddenException("해당 템플릿에 대한 권한이 없습니다");
        }
    }
}
