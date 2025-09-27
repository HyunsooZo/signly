package com.signly.template.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signly.common.exception.ValidationException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        try {
            List<SectionPayload> payload = MAPPER.readValue(jsonContent, new TypeReference<>() {});
            if (payload.isEmpty()) {
                throw new ValidationException("최소 한 개 이상의 섹션이 필요합니다");
            }
            return payload.stream()
                    .map(SectionPayload::toDomain)
                    .sorted(Comparator.comparingInt(TemplateSection::getOrder))
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (JsonProcessingException e) {
            throw new ValidationException("유효하지 않은 템플릿 섹션 형식입니다");
        }
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
