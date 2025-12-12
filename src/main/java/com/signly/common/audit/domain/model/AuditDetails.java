package com.signly.common.audit.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;

/**
 * 감사 로그 상세 정보 (Value Object)
 * 변경 전/후 데이터와 메타데이터를 JSON 형태로 저장
 */
@Getter
public class AuditDetails {
    private final Map<String, Object> before;
    private final Map<String, Object> after;
    private final Map<String, Object> metadata;

    private AuditDetails(
            Map<String, Object> before,
            Map<String, Object> after,
            Map<String, Object> metadata
    ) {
        this.before = before != null ? Map.copyOf(before) : Collections.emptyMap();
        this.after = after != null ? Map.copyOf(after) : Collections.emptyMap();
        this.metadata = metadata != null ? Map.copyOf(metadata) : Collections.emptyMap();
    }

    public static AuditDetails of(
            Map<String, Object> before,
            Map<String, Object> after,
            Map<String, Object> metadata
    ) {
        return new AuditDetails(before, after, metadata);
    }

    public static AuditDetails empty() {
        return new AuditDetails(null, null, null);
    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(Map.of(
                    "before", before,
                    "after", after,
                    "metadata", metadata
            ));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("감사 로그 JSON 직렬화 실패", e);
        }
    }

    public static AuditDetails fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return empty();
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = mapper.readValue(json, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> before = (Map<String, Object>) data.getOrDefault("before", Collections.emptyMap());
            @SuppressWarnings("unchecked")
            Map<String, Object> after = (Map<String, Object>) data.getOrDefault("after", Collections.emptyMap());
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) data.getOrDefault("metadata", Collections.emptyMap());

            return new AuditDetails(before, after, metadata);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("감사 로그 JSON 역직렬화 실패", e);
        }
    }
}