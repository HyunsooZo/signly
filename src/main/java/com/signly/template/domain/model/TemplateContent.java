package com.signly.template.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signly.common.exception.ValidationException;
import com.signly.common.util.UlidGenerator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TemplateContent {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String VERSION = "1.0";

    private final String version;
    private final TemplateMetadata metadata;
    private final List<TemplateSection> sections;

    private TemplateContent(String version,
                            TemplateMetadata metadata,
                            List<TemplateSection> sections) {
        this.version = version;
        this.metadata = metadata;
        this.sections = sections != null ? new ArrayList<>(sections) : new ArrayList<>();
    }

    public static TemplateContent fromJson(String jsonContent) {
        if (jsonContent == null || jsonContent.trim().isEmpty()) {
            throw new ValidationException("템플릿 내용은 필수입니다");
        }

        try {
            JsonNode root = MAPPER.readTree(jsonContent);

            String version = root.has("version") ? root.get("version").asText() : VERSION;

            TemplateMetadata metadata = parseMetadata(root.get("metadata"));

            List<TemplateSection> sections = parseSections(root.get("sections"));

            return new TemplateContent(version, metadata, sections);
        } catch (JsonProcessingException e) {
            throw new ValidationException("JSON 파싱 실패: " + e.getMessage());
        }
    }

    public static TemplateContent of(TemplateMetadata metadata, List<TemplateSection> sections) {
        if (metadata == null) {
            throw new ValidationException("템플릿 메타데이터는 필수입니다");
        }
        if (sections == null || sections.isEmpty()) {
            throw new ValidationException("최소 한 개 이상의 섹션이 필요합니다");
        }

        List<TemplateSection> sortedSections = sections.stream()
                .sorted(Comparator.comparingInt(TemplateSection::getOrder))
                .collect(Collectors.toCollection(ArrayList::new));

        return new TemplateContent(VERSION, metadata, sortedSections);
    }

    public String toJson() {
        try {
            Map<String, Object> root = new HashMap<>();
            root.put("version", version);
            root.put("metadata", buildMetadataMap());
            root.put("sections", buildSectionsArray());

            return MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new ValidationException("JSON 직렬화 실패: " + e.getMessage());
        }
    }

    private static TemplateMetadata parseMetadata(JsonNode metadataNode) {
        if (metadataNode == null || metadataNode.isNull()) {
            return TemplateMetadata.of("", "", "", new HashMap<>());
        }

        String title = metadataNode.has("title") ? metadataNode.get("title").asText() : "";
        String description = metadataNode.has("description") ? metadataNode.get("description").asText() : "";
        String createdBy = metadataNode.has("createdBy") ? metadataNode.get("createdBy").asText() : "";

        Map<String, TemplateVariable> variables = new HashMap<>();
        if (metadataNode.has("variables") && metadataNode.get("variables").isObject()) {
            JsonNode variablesNode = metadataNode.get("variables");
            variablesNode.fields().forEachRemaining(entry -> {
                String varName = entry.getKey();
                JsonNode varNode = entry.getValue();

                String label = varNode.has("label") ? varNode.get("label").asText() : varName;
                String typeStr = varNode.has("type") ? varNode.get("type").asText() : "TEXT";
                boolean required = varNode.has("required") && varNode.get("required").asBoolean();
                String defaultValue = varNode.has("defaultValue") ? varNode.get("defaultValue").asText() : "";

                TemplateVariableType type;
                try {
                    type = TemplateVariableType.valueOf(typeStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    type = TemplateVariableType.TEXT;
                }

                variables.put(varName, TemplateVariable.of(label, type, required, defaultValue));
            });
        }

        return TemplateMetadata.of(title, description, createdBy, variables);
    }

    private static List<TemplateSection> parseSections(JsonNode sectionsNode) {
        if (sectionsNode == null || !sectionsNode.isArray()) {
            throw new ValidationException("sections 배열이 필요합니다");
        }

        List<TemplateSection> sections = new ArrayList<>();
        int index = 0;
        for (JsonNode sectionNode : sectionsNode) {
            String sectionId = sectionNode.has("sectionId") ?
                    sectionNode.get("sectionId").asText() :
                    UlidGenerator.generate();

            String typeStr = sectionNode.has("type") ? sectionNode.get("type").asText() : "PARAGRAPH";
            TemplateSectionType type;
            try {
                type = TemplateSectionType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                type = TemplateSectionType.PARAGRAPH;
            }

            int order = sectionNode.has("order") ? sectionNode.get("order").asInt() : index;
            String content = sectionNode.has("content") ? sectionNode.get("content").asText() : "";

            Map<String, Object> metadata = new HashMap<>();
            if (sectionNode.has("metadata") && sectionNode.get("metadata").isObject()) {
                metadata = MAPPER.convertValue(sectionNode.get("metadata"), new TypeReference<>() {});
            }

            List<String> variables = new ArrayList<>();
            if (sectionNode.has("variables") && sectionNode.get("variables").isArray()) {
                for (JsonNode varNode : sectionNode.get("variables")) {
                    variables.add(varNode.asText());
                }
            }

            sections.add(TemplateSection.of(sectionId, type, order, content, metadata, variables));
            index++;
        }

        return sections;
    }

    private Map<String, Object> buildMetadataMap() {
        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("title", metadata.getTitle());
        metadataMap.put("description", metadata.getDescription());
        metadataMap.put("createdBy", metadata.getCreatedBy());

        Map<String, Map<String, Object>> variablesMap = new HashMap<>();
        metadata.getVariables().forEach((key, var) -> {
            Map<String, Object> varMap = new HashMap<>();
            varMap.put("label", var.getLabel());
            varMap.put("type", var.getType().name().toLowerCase());
            varMap.put("required", var.isRequired());
            varMap.put("defaultValue", var.getDefaultValue());
            variablesMap.put(key, varMap);
        });
        metadataMap.put("variables", variablesMap);

        return metadataMap;
    }

    private List<Map<String, Object>> buildSectionsArray() {
        return sections.stream()
                .sorted(Comparator.comparingInt(TemplateSection::getOrder))
                .map(section -> {
                    Map<String, Object> sectionMap = new HashMap<>();
                    sectionMap.put("sectionId", section.getSectionId());
                    sectionMap.put("type", section.getType().name());
                    sectionMap.put("order", section.getOrder());
                    sectionMap.put("content", section.getContent());
                    sectionMap.put("metadata", section.getMetadata());
                    sectionMap.put("variables", section.getVariables());
                    return sectionMap;
                })
                .collect(Collectors.toList());
    }

    public String getVersion() {
        return version;
    }

    public TemplateMetadata getMetadata() {
        return metadata;
    }

    public List<TemplateSection> getSections() {
        return new ArrayList<>(sections);
    }

    public String getJsonContent() {
        return toJson();
    }

    public String renderHtml() {
        return sections.stream()
                .sorted(Comparator.comparingInt(TemplateSection::getOrder))
                .map(section -> renderSectionToHtml(section))
                .collect(Collectors.joining("\n"));
    }

    public String toPlainText() {
        return sections.stream()
                .sorted(Comparator.comparingInt(TemplateSection::getOrder))
                .map(TemplateSection::getContent)
                .filter(text -> text != null && !text.isBlank())
                .collect(Collectors.joining(" • "));
    }

    private String renderSectionToHtml(TemplateSection section) {
        String content = section.getContent();
        return "<section class=\"template-section\" data-type=\"" + section.getType() + "\">" +
                "<p>" + content + "</p></section>";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateContent that = (TemplateContent) o;
        return Objects.equals(version, that.version) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(sections, that.sections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, metadata, sections);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
