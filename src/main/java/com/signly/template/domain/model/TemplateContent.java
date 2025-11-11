package com.signly.template.domain.model;

import com.signly.common.exception.ValidationException;
import com.signly.template.domain.service.TemplateContentParser;
import com.signly.template.domain.service.TemplateContentRenderer;
import com.signly.template.domain.service.TemplateContentSerializer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 템플릿 콘텐츠 도메인 모델
 * SRP: 데이터 보관 및 기본 비즈니스 로직만 담당
 * 파싱, 직렬화, 렌더링은 각각의 서비스 클래스에 위임
 */
public record TemplateContent(
        String version,
        TemplateMetadata metadata,
        List<TemplateSection> sections
) {

    private static final String VERSION = "1.0";
    private static final TemplateContentParser parser = new TemplateContentParser();
    private static final TemplateContentSerializer serializer = new TemplateContentSerializer();
    private static final TemplateContentRenderer renderer = new TemplateContentRenderer();

    /**
     * JSON 문자열로부터 TemplateContent 생성
     */
    public static TemplateContent fromJson(String jsonContent) {
        TemplateContentParser.TemplateContentData data = parser.parseFromJson(jsonContent);
        return new TemplateContent(data.version(), data.metadata(), data.sections());
    }

    /**
     * 메타데이터와 섹션 목록으로 TemplateContent 생성
     */
    public static TemplateContent of(
            TemplateMetadata metadata,
            List<TemplateSection> sections
    ) {
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

    /**
     * JSON 문자열로 변환
     */
    public String toJson() {
        return serializer.serializeToJson(version, metadata, sections);
    }

    /**
     * HTML로 렌더링
     */
    public String renderHtml() {
        return renderer.renderToHtml(sections);
    }

    /**
     * 일반 텍스트로 렌더링
     */
    public String toPlainText() {
        return renderer.renderToPlainText(sections);
    }

    public List<TemplateSection> sections() {
        return new ArrayList<>(sections);
    }

    public String jsonContent() {
        return toJson();
    }
}
