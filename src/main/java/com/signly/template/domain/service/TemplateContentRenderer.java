package com.signly.template.domain.service;

import com.signly.template.domain.model.TemplateSection;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 템플릿 콘텐츠 렌더링 서비스
 * SRP: 템플릿 콘텐츠를 다양한 형식으로 렌더링 담당
 */
public class TemplateContentRenderer {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\[([^\\]]+)\\]");

    /**
     * 섹션 목록을 HTML로 렌더링
     */
    public String renderToHtml(List<TemplateSection> sections) {
        return sections.stream()
                .sorted(java.util.Comparator.comparingInt(TemplateSection::getOrder))
                .map(this::renderSectionToHtml)
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

    /**
     * 개별 섹션을 HTML로 렌더링
     */
    private String renderSectionToHtml(TemplateSection section) {
        String content = section.getContent();
        content = convertVariablesToUnderlines(content);
        return "<section class=\"template-section\" data-type=\"" + section.getType() + "\">" +
                "<p>" + content + "</p></section>";
    }

    /**
     * 변수 [변수명]을 빈 밑줄로 변환 (서명 이미지 변수 제외)
     */
    private String convertVariablesToUnderlines(String content) {
        if (content == null) {
            return "";
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            
            // 서명 이미지 변수는 밑줄로 변환하지 않음
            if (variableName.endsWith("_SIGNATURE_IMAGE")) {
                matcher.appendReplacement(result, matcher.group(0));
                continue;
            }
            
            String replacement = "<span class=\"blank-line\" data-variable-name=\"" + 
                               escapeHtml(variableName) + "\"></span>";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    /**
     * HTML 특수문자 이스케이프
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}