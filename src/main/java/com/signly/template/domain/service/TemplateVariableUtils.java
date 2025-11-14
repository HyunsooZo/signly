package com.signly.template.domain.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 템플릿 변수 처리 유틸리티
 * SRP: 템플릿 변수 변환, 이스케이프, 대입 등 공통 로직 담당
 */
@Component
public class TemplateVariableUtils {
    
    // 표준 변수 패턴 [VARIABLE_NAME]
    private static final Pattern STANDARD_VARIABLE_PATTERN = 
        Pattern.compile("\\[([^\\]]+)\\]");
    
    /**
     * HTML 특수문자 이스케이프
     * @param text 이스케이프할 텍스트
     * @return 이스케이프된 텍스트
     */
    public static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    /**
     * 변수를 밑줄로 변환 (미리보기용)
     * @param content 템플릿 콘텐츠
     * @return 밑줄로 변환된 HTML
     */
    public static String convertVariablesToUnderlines(String content) {
        return convertVariablesToUnderlines(content, false);
    }
    
    /**
     * 변수를 밑줄로 변환 (서명 이미지 제외 옵션)
     * @param content 템플릿 콘텐츠
     * @param excludeSignatureImages 서명 이미지 변수 제외 여부
     * @return 밑줄로 변환된 HTML
     */
    public static String convertVariablesToUnderlines(String content, boolean excludeSignatureImages) {
        if (content == null) {
            return "";
        }
        
        Matcher matcher = STANDARD_VARIABLE_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            
            // 서명 이미지 변수 제외
            if (excludeSignatureImages && variableName.endsWith("_SIGNATURE_IMAGE")) {
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
     * 변수 값 대입 (계약서 생성용)
     * @param content 템플릿 콘텐츠
     * @param variableValues 변수 값 맵
     * @return 값이 대입된 HTML
     */
    public static String substituteVariables(String content, Map<String, String> variableValues) {
        if (content == null) {
            return "";
        }
        
        Matcher matcher = STANDARD_VARIABLE_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            String value = variableValues.getOrDefault(variableName, "[" + variableName + "]");
            matcher.appendReplacement(result, Matcher.quoteReplacement(escapeHtml(value)));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * 템플릿 콘텐츠에서 모든 변수명 추출
     * @param content 템플릿 콘텐츠
     * @return 변수명 집합
     */
    public static java.util.Set<String> extractVariableNames(String content) {
        if (content == null) {
            return java.util.Collections.emptySet();
        }
        
        java.util.Set<String> variableNames = new java.util.HashSet<>();
        Matcher matcher = STANDARD_VARIABLE_PATTERN.matcher(content);
        
        while (matcher.find()) {
            variableNames.add(matcher.group(1));
        }
        
        return variableNames;
    }
    
    /**
     * 템플릿 콘텐츠에 변수가 포함되어 있는지 확인
     * @param content 템플릿 콘텐츠
     * @return 변수 포함 여부
     */
    public static boolean hasVariables(String content) {
        if (content == null) {
            return false;
        }
        return STANDARD_VARIABLE_PATTERN.matcher(content).find();
    }
}