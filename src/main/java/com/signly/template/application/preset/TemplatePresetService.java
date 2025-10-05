package com.signly.template.application.preset;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signly.template.infrastructure.repository.TemplateJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TemplatePresetService {

    private final TemplateJpaRepository templateRepository;
    private final ObjectMapper objectMapper;

    public TemplatePresetService(TemplateJpaRepository templateRepository, ObjectMapper objectMapper) {
        this.templateRepository = templateRepository;
        this.objectMapper = objectMapper;
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
                    try {
                        List<Map<String, Object>> sectionsData = objectMapper.readValue(
                                entity.getContent(),
                                new TypeReference<List<Map<String, Object>>>() {}
                        );

                        List<PresetSection> sections = sectionsData.stream()
                                .map(data -> {
                                    Map<String, Object> metadata = (Map<String, Object>) data.get("metadata");
                                    return new PresetSection(
                                            (String) data.get("sectionId"),
                                            (String) data.get("type"),
                                            ((Number) data.get("order")).intValue(),
                                            (String) data.get("content"),
                                            metadata != null ? metadata : Map.of()
                                    );
                                })
                                .toList();

                        return new TemplatePreset(
                                entity.getPresetId(),
                                entity.getTitle(),
                                "프리셋 템플릿",
                                sections
                        );
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse preset template: " + presetId, e);
                    }
                });
    }
}
