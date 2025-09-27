package com.signly.template.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.signly.template.domain.model.TemplateStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Getter
public class TemplateResponse {
    private final String templateId;
    private final String ownerId;
    private final String title;
    private final String sectionsJson;
    private final int version;
    private final TemplateStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime updatedAt;
    private final List<TemplateSectionDto> sections;
    private final String renderedHtml;
    private final String previewText;

    @JsonCreator
    public TemplateResponse(
            @JsonProperty("templateId") String templateId,
            @JsonProperty("ownerId") String ownerId,
            @JsonProperty("title") String title,
            @JsonProperty("sectionsJson") String sectionsJson,
            @JsonProperty("version") int version,
            @JsonProperty("status") TemplateStatus status,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("updatedAt") LocalDateTime updatedAt,
            @JsonProperty("sections") List<TemplateSectionDto> sections,
            @JsonProperty("renderedHtml") String renderedHtml,
            @JsonProperty("previewText") String previewText
    ) {
        this.templateId = templateId;
        this.ownerId = ownerId;
        this.title = title;
        this.sectionsJson = sectionsJson;
        this.version = version;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.sections = sections;
        this.renderedHtml = renderedHtml;
        this.previewText = previewText;
    }

    @JsonProperty("content")
    public String getContent() {
        return sectionsJson;
    }

    @JsonIgnore
    public Date getCreatedAtDate() {
        return toDate(createdAt);
    }

    @JsonIgnore
    public Date getUpdatedAtDate() {
        return toDate(updatedAt);
    }

    private Date toDate(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }
}
