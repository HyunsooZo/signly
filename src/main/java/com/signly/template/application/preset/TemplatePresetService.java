package com.signly.template.application.preset;

import com.signly.template.domain.model.TemplateContent;
import com.signly.template.domain.model.TemplateSection;
import com.signly.template.infrastructure.entity.TemplateEntity;
import com.signly.template.infrastructure.repository.TemplateJpaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TemplatePresetService {

    private final TemplateJpaRepository templateRepository;

    public TemplatePresetService(TemplateJpaRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public List<TemplatePresetSummary> getSummaries() {
        return templateRepository.findAllActivePresets().stream()
                .map(entity -> new TemplatePresetSummary(
                        entity.getPresetId(),
                        entity.getTitle(),
                        "프리셋 템플릿" // description은 별도 필드로 추가 가능
                ))
                .toList();
    }

    public Optional<TemplatePreset> getPreset(String presetId) {
        return templateRepository.findByIsPresetTrueAndPresetId(presetId)
                .map(entity -> {
                    List<PresetSection> sections = parsePresetSections(entity);
                    return new TemplatePreset(
                            entity.getPresetId(),
                            entity.getTitle(),
                            "프리셋 템플릿",
                            sections
                    );
                });
    }

    private List<PresetSection> parsePresetSections(TemplateEntity entity) {
        try {
            // TemplateContent 도메인 모델을 사용하여 파싱
            TemplateContent templateContent = TemplateContent.fromJson(entity.getContent());
            
            // TemplateSection을 PresetSection으로 변환
            return templateContent.getSections().stream()
                    .map(this::convertToPresetSection)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse preset template: " + entity.getPresetId(), e);
        }
    }

    private PresetSection convertToPresetSection(TemplateSection templateSection) {
        return new PresetSection(
                templateSection.getSectionId(),
                templateSection.getType().name(),
                templateSection.getOrder(),
                templateSection.getContent(),
                templateSection.getMetadata()
        );
    }
}
