package com.signly.template.application;

import com.signly.template.application.dto.CreateTemplateCommand;
import com.signly.template.application.dto.TemplateResponse;
import com.signly.template.application.dto.UpdateTemplateCommand;
import com.signly.template.application.mapper.TemplateDtoMapper;
import com.signly.common.exception.ForbiddenException;
import com.signly.common.exception.NotFoundException;
import com.signly.common.exception.ValidationException;
import com.signly.template.domain.model.ContractTemplate;
import com.signly.template.domain.model.TemplateContent;
import com.signly.template.domain.model.TemplateId;
import com.signly.template.domain.model.TemplateStatus;
import com.signly.template.domain.repository.TemplateRepository;
import com.signly.user.domain.model.User;
import com.signly.user.domain.model.UserId;
import com.signly.user.domain.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final TemplateDtoMapper templateDtoMapper;

    public TemplateService(TemplateRepository templateRepository, UserRepository userRepository, TemplateDtoMapper templateDtoMapper) {
        this.templateRepository = templateRepository;
        this.userRepository = userRepository;
        this.templateDtoMapper = templateDtoMapper;
    }

    public TemplateResponse createTemplate(String userId, CreateTemplateCommand command) {
        UserId userIdObj = UserId.of(userId);
        User user = userRepository.findById(userIdObj)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        if (!user.canCreateTemplate()) {
            throw new ForbiddenException("템플릿을 생성할 권한이 없습니다");
        }

        if (templateRepository.existsByOwnerIdAndTitle(userIdObj, command.title())) {
            throw new ValidationException("이미 같은 제목의 템플릿이 존재합니다");
        }

        TemplateContent content = TemplateContent.of(command.content());
        ContractTemplate template = ContractTemplate.create(userIdObj, command.title(), content);

        ContractTemplate savedTemplate = templateRepository.save(template);
        return templateDtoMapper.toResponse(savedTemplate);
    }

    @CachePut(value = "templates", key = "#templateId")
    @CacheEvict(value = "activeTemplates", key = "#userId")
    public TemplateResponse updateTemplate(String userId, String templateId, UpdateTemplateCommand command) {
        TemplateId templateIdObj = TemplateId.of(templateId);
        ContractTemplate template = templateRepository.findById(templateIdObj)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다"));

        validateOwnership(userId, template);

        if (!command.title().equals(template.getTitle()) &&
            templateRepository.existsByOwnerIdAndTitle(template.getOwnerId(), command.title())) {
            throw new ValidationException("이미 같은 제목의 템플릿이 존재합니다");
        }

        template.updateTitle(command.title());
        TemplateContent newContent = TemplateContent.of(command.content());
        template.updateContent(newContent);

        ContractTemplate updatedTemplate = templateRepository.save(template);
        return templateDtoMapper.toResponse(updatedTemplate);
    }

    @CacheEvict(value = {"templates", "activeTemplates"}, key = "#templateId")
    public void activateTemplate(String userId, String templateId) {
        TemplateId templateIdObj = TemplateId.of(templateId);
        ContractTemplate template = templateRepository.findById(templateIdObj)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다"));

        validateOwnership(userId, template);
        template.activate();
        templateRepository.save(template);
    }

    @CacheEvict(value = {"templates", "activeTemplates"}, key = "#templateId")
    public void archiveTemplate(String userId, String templateId) {
        TemplateId templateIdObj = TemplateId.of(templateId);
        ContractTemplate template = templateRepository.findById(templateIdObj)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다"));

        validateOwnership(userId, template);
        template.archive();
        templateRepository.save(template);
    }

    @CacheEvict(value = {"templates", "activeTemplates"}, key = "#templateId")
    public void deleteTemplate(String userId, String templateId) {
        TemplateId templateIdObj = TemplateId.of(templateId);
        ContractTemplate template = templateRepository.findById(templateIdObj)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다"));

        validateOwnership(userId, template);

        if (!template.canDelete()) {
            throw new ValidationException("DRAFT 상태의 템플릿만 삭제할 수 있습니다");
        }

        templateRepository.delete(template);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "templates", key = "#templateId")
    public TemplateResponse getTemplate(String userId, String templateId) {
        TemplateId templateIdObj = TemplateId.of(templateId);
        ContractTemplate template = templateRepository.findById(templateIdObj)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다"));

        validateOwnership(userId, template);
        return templateDtoMapper.toResponse(template);
    }

    @Transactional(readOnly = true)
    public Page<TemplateResponse> getTemplatesByOwner(String userId, Pageable pageable) {
        UserId userIdObj = UserId.of(userId);
        Page<ContractTemplate> templates = templateRepository.findByOwnerId(userIdObj, pageable);
        return templates.map(templateDtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TemplateResponse> getTemplatesByOwnerAndStatus(String userId, TemplateStatus status, Pageable pageable) {
        UserId userIdObj = UserId.of(userId);
        Page<ContractTemplate> templates = templateRepository.findByOwnerIdAndStatus(userIdObj, status, pageable);
        return templates.map(templateDtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "activeTemplates", key = "#userId")
    public List<TemplateResponse> getActiveTemplates(String userId) {
        UserId userIdObj = UserId.of(userId);
        List<ContractTemplate> activeTemplates = templateRepository.findActiveTemplatesByOwnerId(userIdObj);
        return activeTemplates.stream()
                .map(templateDtoMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validateOwnership(String userId, ContractTemplate template) {
        if (!template.getOwnerId().getValue().equals(userId)) {
            throw new ForbiddenException("해당 템플릿에 대한 권한이 없습니다");
        }
    }
}