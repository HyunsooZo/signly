package com.signly.template.application;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final TemplateDtoMapper templateDtoMapper;

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
        return templateDtoMapper.toResponse(updatedTemplate);
    }

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
    }

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
    }

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
    }

    @Transactional(readOnly = true)
    public TemplateResponse getTemplate(
            String userId,
            String templateId
    ) {
        var templateIdObj = TemplateId.of(templateId);
        var template = templateRepository.findById(templateIdObj)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다"));

        validateOwnership(userId, template);
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

    @Transactional(readOnly = true)
    public List<TemplateResponse> getActiveTemplates(String userId) {
        var userIdObj = UserId.of(userId);
        var activeTemplates = templateRepository.findActiveTemplatesByOwnerId(userIdObj);
        return activeTemplates.stream()
                .map(templateDtoMapper::toResponse)
                .collect(Collectors.toList());
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
