package com.deally.template.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateVariableUtilsTest {

    @Test
    @DisplayName("HTML 이스케이프 테스트")
    void escapeHtmlTest() {
        // Given
        String input = "<script>alert('xss')</script>";
        
        // When
        String result = TemplateVariableUtils.escapeHtml(input);
        
        // Then
        assertThat(result).isEqualTo("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;");
    }

    @Test
    @DisplayName("null 입력 시 빈 문자열 반환")
    void escapeHtmlNullTest() {
        // When
        String result = TemplateVariableUtils.escapeHtml(null);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("변수 밑줄 변환 테스트")
    void convertVariablesToUnderlinesTest() {
        // Given
        String input = "안녕하세요 [NAME]님, 주소는 [ADDRESS]입니다.";
        
        // When
        String result = TemplateVariableUtils.convertVariablesToUnderlines(input);
        
        // Then
        assertThat(result).contains("blank-line")
                          .contains("data-variable-name=\"NAME\"")
                          .contains("data-variable-name=\"ADDRESS\"");
    }

    @Test
    @DisplayName("서명 이미지 변수 제외 테스트")
    void excludeSignatureImagesTest() {
        // Given
        String input = "[EMPLOYEE] [EMPLOYEE_SIGNATURE_IMAGE]";
        
        // When
        String result = TemplateVariableUtils.convertVariablesToUnderlines(input, true);
        
        // Then
        assertThat(result).contains("blank-line")
                          .contains("[EMPLOYEE_SIGNATURE_IMAGE]");
    }

    @Test
    @DisplayName("변수 값 대입 테스트")
    void substituteVariablesTest() {
        // Given
        String input = "안녕하세요 [NAME]님";
        Map<String, String> values = Map.of("NAME", "홍길동");
        
        // When
        String result = TemplateVariableUtils.substituteVariables(input, values);
        
        // Then
        assertThat(result).isEqualTo("안녕하세요 홍길동님");
    }

    @Test
    @DisplayName("변수 값이 없을 경우 원형 유지 테스트")
    void substituteVariablesMissingTest() {
        // Given
        String input = "안녕하세요 [NAME]님";
        Map<String, String> values = Map.of();
        
        // When
        String result = TemplateVariableUtils.substituteVariables(input, values);
        
        // Then
        assertThat(result).isEqualTo("안녕하세요 [NAME]님");
    }

    @Test
    @DisplayName("변수명 추출 테스트")
    void extractVariableNamesTest() {
        // Given
        String input = "[NAME]님의 [ADDRESS]에서 [COMPANY] 근무";
        
        // When
        var result = TemplateVariableUtils.extractVariableNames(input);
        
        // Then
        assertThat(result).containsExactlyInAnyOrder("NAME", "ADDRESS", "COMPANY");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "일반 텍스트", "변수 없음"})
    @DisplayName("변수 포함 여부 확인 테스트 - 변수 없음")
    void hasVariablesFalseTest(String input) {
        // When
        boolean result = TemplateVariableUtils.hasVariables(input);
        
        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("변수 포함 여부 확인 테스트 - 변수 있음")
    void hasVariablesTrueTest() {
        // Given
        String input = "[NAME]님 안녕하세요";
        
        // When
        boolean result = TemplateVariableUtils.hasVariables(input);
        
        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("null 입력 시 변수 포함 여부 테스트")
    void hasVariablesNullTest() {
        // When
        boolean result = TemplateVariableUtils.hasVariables(null);
        
        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("복잡한 변수 변환 테스트")
    void complexVariableConversionTest() {
        // Given
        String input = "계약서: [CONTRACT_TITLE]\n" +
                       "당사자: [PARTY_A], [PARTY_B]\n" +
                       "서명: [PARTY_A_SIGNATURE_IMAGE], [PARTY_B_SIGNATURE_IMAGE]";
        
        // When
        String result = TemplateVariableUtils.convertVariablesToUnderlines(input, true);
        
        // Then
        assertThat(result).contains("data-variable-name=\"CONTRACT_TITLE\"")
                          .contains("data-variable-name=\"PARTY_A\"")
                          .contains("data-variable-name=\"PARTY_B\"")
                          .contains("[PARTY_A_SIGNATURE_IMAGE]")
                          .contains("[PARTY_B_SIGNATURE_IMAGE]");
    }
}