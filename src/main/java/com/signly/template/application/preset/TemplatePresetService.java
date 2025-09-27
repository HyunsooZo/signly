package com.signly.template.application.preset;

import com.signly.template.domain.model.TemplateSectionType;
import org.springframework.util.StreamUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TemplatePresetService {

    private final Map<String, TemplatePreset> presets;

    public TemplatePresetService(ResourceLoader resourceLoader) {
        try {
            this.presets = Map.copyOf(loadPresets(resourceLoader));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to initialize template presets", e);
        }
    }

    public List<TemplatePresetSummary> getSummaries() {
        return presets.values().stream()
                .map(preset -> new TemplatePresetSummary(preset.id(), preset.name(), preset.description()))
                .toList();
    }

    public Optional<TemplatePreset> getPreset(String presetId) {
        return Optional.ofNullable(presets.get(presetId));
    }

    private Map<String, TemplatePreset> loadPresets(ResourceLoader resourceLoader) throws IOException {
        Map<String, TemplatePreset> map = new LinkedHashMap<>();

        map.put("standard-employment-contract", loadStandardEmploymentContract(resourceLoader));

        return map;
    }

    private TemplatePreset loadStandardEmploymentContract(ResourceLoader resourceLoader) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:presets/templates/standard-employment-contract.html");
        String html;
        try (var inputStream = resource.getInputStream()) {
            html = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }

        PresetSection section = new PresetSection(
                "preset-standard-employment-contract",
                TemplateSectionType.CUSTOM.name(),
                0,
                html,
                Map.of(
                        "rawHtml", true,
                        "preset", true,
                        "title", "표준근로계약서"
                )
        );

        return new TemplatePreset(
                "standard-employment-contract",
                "표준 근로계약서",
                "고용노동부 표준 근로계약서 양식",
                List.of(section)
        );
    }
}
