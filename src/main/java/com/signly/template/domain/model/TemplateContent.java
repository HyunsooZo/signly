package com.signly.template.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.signly.common.exception.ValidationException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class TemplateContent {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String jsonContent;
    private final List<TemplateSection> sections;

    private TemplateContent(String jsonContent, List<TemplateSection> sections) {
        this.jsonContent = jsonContent;
        this.sections = sections;
    }

    public static TemplateContent of(String jsonContent) {
        if (jsonContent == null || jsonContent.trim().isEmpty()) {
            throw new ValidationException("템플릿 내용은 필수입니다");
        }
        List<TemplateSection> parsedSections = parseJson(jsonContent);
        String normalizedJson = toJson(parsedSections);
        return new TemplateContent(normalizedJson, parsedSections);
    }

    public static TemplateContent fromSections(List<TemplateSection> sections) {
        if (sections == null || sections.isEmpty()) {
            throw new ValidationException("최소 한 개 이상의 섹션이 필요합니다");
        }
        List<TemplateSection> normalized = sections.stream()
                .sorted(Comparator.comparingInt(TemplateSection::getOrder))
                .collect(Collectors.toCollection(ArrayList::new));
        String json = toJson(normalized);
        return new TemplateContent(json, normalized);
    }

    public List<TemplateSection> getSections() {
        return sections;
    }

    public String getJsonContent() {
        return jsonContent;
    }

    public String renderHtml() {
        return sections.stream()
                .sorted(Comparator.comparingInt(TemplateSection::getOrder))
                .map(TemplateSection::renderHtml)
                .collect(Collectors.joining("\n"));
    }

    public String toPlainText() {
        return sections.stream()
                .sorted(Comparator.comparingInt(TemplateSection::getOrder))
                .map(TemplateSection::toPlainText)
                .filter(text -> !text.isBlank())
                .collect(Collectors.joining(" \u2022 "));
    }

    private static List<TemplateSection> parseJson(String jsonContent) {
        String trimmed = jsonContent.trim();

        if (trimmed.isEmpty()) {
            throw new ValidationException("템플릿 내용은 필수입니다");
        }

        try {
            JsonNode root = MAPPER.readTree(trimmed);
            List<TemplateSection> sections = new ArrayList<>();

            if (root.isArray()) {
                sections.addAll(convertArray((ArrayNode) root));
            } else if (root.isObject()) {
                if (root.has("sections") && root.get("sections").isArray()) {
                    sections.addAll(convertArray((ArrayNode) root.get("sections")));
                } else {
                    sections.add(convertNode(root, 0));
                }
            } else if (root.isTextual()) {
                sections.add(createLegacySection(root.asText(), 0));
            }

            if (!sections.isEmpty()) {
                sections.sort(Comparator.comparingInt(TemplateSection::getOrder));
                return sections;
            }
        } catch (JsonProcessingException ignored) {
            // Fallback 처리
        }

        return List.of(createLegacySection(jsonContent, 0));
    }

    private static String toJson(List<TemplateSection> sections) {
        try {
            List<SectionPayload> payload = sections.stream()
                    .map(SectionPayload::fromDomain)
                    .collect(Collectors.toList());
            return MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new ValidationException("템플릿 섹션을 직렬화할 수 없습니다");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateContent that = (TemplateContent) o;
        return Objects.equals(jsonContent, that.jsonContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonContent);
    }

    @Override
    public String toString() {
        return jsonContent;
    }

    private static List<TemplateSection> convertArray(ArrayNode arrayNode) {
        List<TemplateSection> sections = new ArrayList<>();
        int index = 0;
        for (JsonNode node : arrayNode) {
            sections.add(convertNode(node, index++));
        }
        return sections;
    }

    private static TemplateSection convertNode(JsonNode node, int fallbackOrder) {
        if (node == null || node.isNull()) {
            return createLegacySection("", fallbackOrder);
        }

        if (node.isTextual()) {
            return createLegacySection(node.asText(), fallbackOrder);
        }

        String sectionId = node.hasNonNull("sectionId") ? node.get("sectionId").asText() : generateSectionId();
        String typeText = node.hasNonNull("type") ? node.get("type").asText() : null;
        TemplateSectionType type;
        try {
            type = typeText == null ? TemplateSectionType.PARAGRAPH : TemplateSectionType.valueOf(typeText);
        } catch (IllegalArgumentException ex) {
            type = TemplateSectionType.PARAGRAPH;
        }

        int order = node.has("order") && node.get("order").canConvertToInt() ? node.get("order").asInt() : fallbackOrder;
        String content = node.hasNonNull("content") ? node.get("content").asText() : node.toString();

        Map<String, Object> metadata = null;
        if (node.has("metadata") && node.get("metadata").isObject()) {
            metadata = MAPPER.convertValue(node.get("metadata"), Map.class);
        }

        return TemplateSection.of(sectionId, type, order, content, metadata == null ? Map.of() : metadata);
    }

    private static TemplateSection createLegacySection(String content, int order) {
        return TemplateSection.of(
                generateSectionId(),
                TemplateSectionType.PARAGRAPH,
                order,
                content,
                Map.of("legacy", true)
        );
    }

    private static String generateSectionId() {
        return "sec-" + UUID.randomUUID();
    }

    private record SectionPayload(
            String sectionId,
            String type,
            Integer order,
            String content,
            Map<String, Object> metadata
    ) {
        private static SectionPayload fromDomain(TemplateSection section) {
            return new SectionPayload(
                    section.getSectionId(),
                    section.getType().name(),
                    section.getOrder(),
                    section.getContent(),
                    section.getMetadata()
            );
        }

        private TemplateSection toDomain() {
            TemplateSectionType sectionType;
            try {
                sectionType = TemplateSectionType.valueOf(type == null ? "PARAGRAPH" : type);
            } catch (IllegalArgumentException e) {
                sectionType = TemplateSectionType.PARAGRAPH;
            }
            return TemplateSection.of(
                    sectionId == null || sectionId.isBlank() ? "sec-" + System.nanoTime() : sectionId,
                    sectionType,
                    order == null ? 0 : order,
                    content == null ? "" : content,
                    metadata
            );
        }
    }
}
