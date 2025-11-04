package com.signly.template.application.preset;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            // rawHtml 체크
            if (isRawHtmlFormat(entity)) {
                // rawHtml 포맷인 경우 원본 HTML을 그대로 반환
                return parseHtmlToSections(entity.getContent());
            } else {
                // 일반 JSON 섹션 포맷
                return parseJsonSections(entity.getContent());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse preset template: " + entity.getPresetId(), e);
        }
    }

    private boolean isRawHtmlFormat(TemplateEntity entity) {
        try {
            List<Map<String, Object>> sectionsData = objectMapper.readValue(
                    entity.getContent(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );
            
            // 단일 섹션에 rawHtml 플래그가 있는지 확인
            if (sectionsData.size() == 1) {
                Map<String, Object> sectionData = sectionsData.get(0);
                Map<String, Object> metadata = (Map<String, Object>) sectionData.get("metadata");
                return metadata != null && Boolean.TRUE.equals(metadata.get("rawHtml"));
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private List<PresetSection> parseJsonSections(String content) throws Exception {
        List<Map<String, Object>> sectionsData = objectMapper.readValue(
                content,
                new TypeReference<List<Map<String, Object>>>() {}
        );

        return sectionsData.stream()
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
    }

    private List<PresetSection> parseHtmlToSections(String jsonContent) {
        List<PresetSection> sections = new ArrayList<>();

        try {
            // JSON에서 content 필드를 추출 (원본 HTML)
            List<Map<String, Object>> sectionsData = objectMapper.readValue(
                    jsonContent,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            if (!sectionsData.isEmpty()) {
                Map<String, Object> firstSection = sectionsData.get(0);
                String htmlContent = (String) firstSection.get("content");

                if (htmlContent != null && !htmlContent.isBlank()) {
                    // 원본 HTML을 그대로 단일 섹션으로 반환
                    Map<String, Object> metadata = (Map<String, Object>) firstSection.get("metadata");
                    if (metadata == null) {
                        metadata = Map.of("rawHtml", true);
                    }

                    sections.add(new PresetSection(
                            "preset-section-0",
                            "CUSTOM",
                            0,
                            htmlContent,
                            metadata
                    ));
                    return sections;
                }
            }

            // 기본 fallback
            sections.add(createSection("text", jsonContent, 0, Map.of("kind", "text")));

        } catch (Exception e) {
            // 실패 시 단일 섹션으로 fallback
            sections.add(createSection("text", jsonContent, 0, Map.of("kind", "text")));
        }

        return sections;
    }
    
    private PresetSection createSection(String type, String content, int order, Map<String, Object> metadata) {
        return new PresetSection(
                "preset-section-" + order,
                type,
                order,
                content,
                metadata
        );
    }
}
