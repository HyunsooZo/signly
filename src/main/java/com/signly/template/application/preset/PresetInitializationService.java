package com.signly.template.application.preset;

import com.signly.template.domain.model.*;
import com.signly.template.domain.model.TemplateSectionType;
import com.signly.template.infrastructure.entity.TemplateEntity;
import com.signly.template.infrastructure.repository.TemplateJpaRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PresetInitializationService {

    private final TemplateJpaRepository templateRepository;
    private final ResourceLoader resourceLoader;

    public PresetInitializationService(
            TemplateJpaRepository templateRepository, 
            ResourceLoader resourceLoader
    ) {
        this.templateRepository = templateRepository;
        this.resourceLoader = resourceLoader;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializePresets() {
        // 표준근로계약서 프리셋 생성 또는 업데이트
        createOrUpdateStandardEmploymentContractPreset();
    }

    private void createOrUpdateStandardEmploymentContractPreset() {
        try {
            Resource resource = resourceLoader.getResource("classpath:presets/templates/standard-employment-contract.html");
            String html;
            try (var inputStream = resource.getInputStream()) {
                html = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            }

            // Parse HTML into TemplateContent using domain model
            TemplateContent templateContent = parseEmploymentContractToTemplateContent(html);
            String jsonContent = templateContent.toJson();

            // 기존 프리셋 찾기
            var existingPreset = templateRepository.findByIsPresetTrueAndPresetId("standard-employment-contract");

            if (existingPreset.isPresent()) {
                // 기존 프리셋 업데이트
                TemplateEntity preset = existingPreset.get();
                preset.setContent(jsonContent);
                preset.setUpdatedAt(LocalDateTime.now());
                templateRepository.save(preset);
                System.out.println("✅ 표준 근로계약서 프리셋이 업데이트되었습니다.");
            } else {
                // 새 프리셋 생성
                TemplateEntity preset = new TemplateEntity(
                        "01JB0000000000000000000001", // ULID 형식의 고정 ID for preset
                        null, // owner_id is null for presets
                        "표준 근로계약서",
                        jsonContent,
                        templateContent.sections().size(),
                        TemplateStatus.ACTIVE,
                        true, // is_preset
                        "standard-employment-contract", // preset_id
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );

                templateRepository.save(preset);
                System.out.println("✅ 표준 근로계약서 프리셋이 생성되었습니다.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load standard employment contract preset", e);
        }
    }

    private TemplateContent parseEmploymentContractToTemplateContent(String html) {
        List<TemplateSection> sections = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        
        // Extract title
        Elements titleElements = doc.select("div.title");
        if (!titleElements.isEmpty()) {
            TemplateSection titleSection = TemplateSection.of(
                "preset-sec-title", 
                TemplateSectionType.HEADER, 
                0, 
                titleElements.first().outerHtml(),
                Map.of("preset", true, "title", "표준근로계약서"),
                new ArrayList<>()
            );
            sections.add(titleSection);
        }
        
        // Extract intro paragraph
        Elements introElements = doc.select("div.contract-intro");
        if (!introElements.isEmpty()) {
            TemplateSection introSection = TemplateSection.of(
                "preset-sec-intro", 
                TemplateSectionType.PARAGRAPH, 
                1, 
                introElements.first().outerHtml(),
                Map.of("preset", true, "title", "서론"),
                new ArrayList<>()
            );
            sections.add(introSection);
        }
        
        // Extract numbered sections (clauses)
        Elements sectionElements = doc.select("div.section");
        int order = 2;
        for (int i = 0; i < sectionElements.size(); i++) {
            Element sectionElement = sectionElements.get(i);
            String sectionHtml = sectionElement.outerHtml();
            
            // Extract section number from the span
            String sectionTitle = "제" + (i + 1) + "조";
            Elements numberSpan = sectionElement.select("span.section-number");
            if (!numberSpan.isEmpty()) {
                String numberText = numberSpan.first().text();
                if (numberText.contains(".")) {
                    sectionTitle = numberText.split("\\.")[0].trim();
                }
            }
            
            TemplateSection clauseSection = TemplateSection.of(
                "preset-sec-clause-" + (i + 1), 
                TemplateSectionType.PARAGRAPH, 
                order++, 
                sectionHtml,
                Map.of("preset", true, "title", sectionTitle, "clauseNumber", i + 1),
                new ArrayList<>()
            );
            sections.add(clauseSection);
        }
        
        // Extract date section
        Elements dateElements = doc.select("div.date-section");
        if (!dateElements.isEmpty()) {
            TemplateSection dateSection = TemplateSection.of(
                "preset-sec-date", 
                TemplateSectionType.PARAGRAPH, 
                order++, 
                dateElements.first().outerHtml(),
                Map.of("preset", true, "title", "계약체결일"),
                new ArrayList<>()
            );
            sections.add(dateSection);
        }
        
        // Extract signature section
        Elements signatureElements = doc.select("div.signature-section");
        if (!signatureElements.isEmpty()) {
            TemplateSection signatureSection = TemplateSection.of(
                "preset-sec-signature", 
                TemplateSectionType.CUSTOM, 
                order, 
                signatureElements.first().outerHtml(),
                Map.of("preset", true, "title", "서명"),
                new ArrayList<>()
            );
            sections.add(signatureSection);
        }
        
        // Create TemplateMetadata for preset
        TemplateMetadata metadata = TemplateMetadata.of(
            "표준 근로계약서",
            "프리셋 템플릿",
            "system",
            new HashMap<>()
        );
        
        return TemplateContent.of(metadata, sections);
    }


}
