package com.signly.template.application.preset;

import com.signly.template.domain.model.TemplateStatus;
import com.signly.template.infrastructure.entity.TemplateEntity;
import com.signly.template.infrastructure.repository.TemplateJpaRepository;
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

@Service
public class PresetInitializationService {

    private final TemplateJpaRepository templateRepository;
    private final ResourceLoader resourceLoader;

    public PresetInitializationService(TemplateJpaRepository templateRepository, ResourceLoader resourceLoader) {
        this.templateRepository = templateRepository;
        this.resourceLoader = resourceLoader;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializePresets() {
        // 표준근로계약서 프리셋이 없으면 생성
        if (templateRepository.findByIsPresetTrueAndPresetId("standard-employment-contract").isEmpty()) {
            createStandardEmploymentContractPreset();
        }
    }

    private void createStandardEmploymentContractPreset() {
        try {
            Resource resource = resourceLoader.getResource("classpath:presets/templates/standard-employment-contract.html");
            String html;
            try (var inputStream = resource.getInputStream()) {
                html = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            }

            // JSON content 생성
            String jsonContent = String.format(
                    "[{\"sectionId\":\"preset-sec-1\",\"type\":\"CUSTOM\",\"order\":0,\"content\":%s,\"metadata\":{\"rawHtml\":true,\"preset\":true,\"title\":\"표준근로계약서\"}}]",
                    escapeJson(html)
            );

            TemplateEntity preset = new TemplateEntity(
                    "01JB0000000000000000000001", // ULID 형식의 고정 ID for preset
                    null, // owner_id is null for presets
                    "표준 근로계약서",
                    jsonContent,
                    1,
                    TemplateStatus.ACTIVE,
                    true, // is_preset
                    "standard-employment-contract", // preset_id
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            templateRepository.save(preset);
            System.out.println("✅ 표준 근로계약서 프리셋이 생성되었습니다.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load standard employment contract preset", e);
        }
    }

    private String escapeJson(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }
}
