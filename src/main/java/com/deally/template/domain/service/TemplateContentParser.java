package com.deally.template.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.deally.common.exception.ValidationException;
import com.deally.common.util.UlidGenerator;
import com.deally.template.domain.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 템플릿 콘텐츠 파싱 서비스
 * SRP: JSON 파싱 및 객체 변환 담당
 */
public class TemplateContentParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String DEFAULT_VERSION = "1.0";

    /**
     * JSON 문자열을 파싱하여 TemplateContentData 객체로 변환
     */
    public TemplateContentData parseFromJson(String jsonContent) {
        if (jsonContent == null || jsonContent.trim().isEmpty()) {
            throw new ValidationException("템플릿 내용은 필수입니다");
        }

        try {
            JsonNode root = MAPPER.readTree(jsonContent);

            String version = root.has("version") ? root.get("version").asText() : DEFAULT_VERSION;
            TemplateMetadata metadata = parseMetadata(root.get("metadata"));
            List<TemplateSection> sections = parseSections(root.get("sections"));

            return new TemplateContentData(version, metadata, sections);
        } catch (JsonProcessingException e) {
            throw new ValidationException("JSON 파싱 실패: " + e.getMessage());
        }
    }

    /**
     * 메타데이터 파싱
     */
    private TemplateMetadata parseMetadata(JsonNode metadataNode) {
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

    /**
     * 섹션 목록 파싱
     */
    private List<TemplateSection> parseSections(JsonNode sectionsNode) {
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

    /**
     * 파싱된 데이터를 담는 DTO
     */
    public record TemplateContentData(
            String version,
            TemplateMetadata metadata,
            List<TemplateSection> sections
    ) {}
}