package com.signly.template.domain.service;

import com.signly.template.domain.model.TemplateSection;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 템플릿 콘텐츠 렌더링 서비스
 * SRP: 템플릿 콘텐츠를 다양한 형식으로 렌더링 담당
 * <p>
 * Refactored to use unified utilities and eliminate duplicate code.
 */
@Service
public class TemplateContentRenderer {

    private final HtmlSectionRenderer sectionRenderer;

    public TemplateContentRenderer(HtmlSectionRenderer sectionRenderer) {
        this.sectionRenderer = sectionRenderer;
    }

    /**
     * 섹션 목록을 HTML로 렌더링 (미리보기 모드)
     */
    public String renderToHtml(List<TemplateSection> sections) {
        return sections.stream()
                .sorted(java.util.Comparator.comparingInt(TemplateSection::getOrder))
                .map(section -> sectionRenderer.renderSection(section, RenderMode.PREVIEW))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 섹션 목록을 일반 텍스트로 렌더링
     */
    public String renderToPlainText(List<TemplateSection> sections) {
        return sections.stream()
                .sorted(java.util.Comparator.comparingInt(TemplateSection::getOrder))
                .map(TemplateSection::getContent)
                .filter(text -> text != null && !text.isBlank())
                .collect(Collectors.joining(" • "));
    }
}