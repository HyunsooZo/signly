package com.signly.template.application.preset;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signly.template.infrastructure.entity.TemplateEntity;
import com.signly.template.infrastructure.repository.TemplateJpaRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
                return parseHtmlToSections(entity.getContent());
            } else {
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

    private List<PresetSection> parseHtmlToSections(String html) {
        List<PresetSection> sections = new ArrayList<>();
        
        try {
            // Jsoup 사용하여 HTML 파싱
            Document doc = Jsoup.parse(html);
            int order = 0;
            
            // title 클래스 → title 섹션
            Elements titleElements = doc.select(".title");
            for (Element element : titleElements) {
                sections.add(createSection("title", element.text(), order++, Map.of("kind", "title")));
            }
            
            // contract-intro 클래스 → text 섹션
            Elements introElements = doc.select(".contract-intro");
            for (Element element : introElements) {
                sections.add(createSection("text", element.text(), order++, Map.of("kind", "text")));
            }
            
            // section 클래스 → clause 섹션 (번호 추출)
            Elements sectionElements = doc.select(".section");
            for (Element element : sectionElements) {
                String content = element.text();
                String type = element.select(".section-number").isEmpty() ? "text" : "clause";
                sections.add(createSection(type, content, order++, Map.of("kind", type)));
            }
            
            // date-section 클래스 → text 섹션
            Elements dateElements = doc.select(".date-section");
            for (Element element : dateElements) {
                sections.add(createSection("text", element.text(), order++, Map.of("kind", "text")));
            }
            
            // signature-section 클래스 → signature 섹션
            Elements signatureElements = doc.select(".signature-section");
            for (Element element : signatureElements) {
                String content = element.html(); // HTML 구조 유지
                sections.add(createSection("signature", content, order++, 
                    Map.of("kind", "signature", "signature", true)));
            }
            
        } catch (Exception e) {
            // 실패 시 단일 섹션으로 fallback
            sections.add(createSection("text", html, 0, Map.of("kind", "text")));
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
