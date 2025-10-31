package com.signly.template.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signly.common.exception.ValidationException;
import com.signly.template.domain.model.TemplateMetadata;
import com.signly.template.domain.model.TemplateSection;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 템플릿 콘텐츠 직렬화 서비스
 * SRP: 객체를 JSON으로 변환 담당
 */
public class TemplateContentSerializer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * TemplateContentData를 JSON 문자열로 직렬화
     */
    public String serializeToJson(String version, TemplateMetadata metadata, List<TemplateSection> sections) {
        try {
            Map<String, Object> root = new HashMap<>();
            root.put("version", version);
            root.put("metadata", buildMetadataMap(metadata));
            root.put("sections", buildSectionsArray(sections));

            return MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new ValidationException("JSON 직렬화 실패: " + e.getMessage());
        }
    }

    /**
     * 메타데이터를 Map으로 변환
     */
    private Map<String, Object> buildMetadataMap(TemplateMetadata metadata) {
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

    /**
     * 섹션 목록을 배열로 변환
     */
    private List<Map<String, Object>> buildSectionsArray(List<TemplateSection> sections) {
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
}