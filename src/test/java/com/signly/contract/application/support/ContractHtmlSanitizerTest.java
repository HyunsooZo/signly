package com.signly.contract.application.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ContractHtmlSanitizerTest {

    @Test
    void sanitize_removesDocumentHeadersWhilePreservingBody() {
        String raw = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>표준근로계약서</title></head><body><div class=\"title\">표준근로계약서</div></body></html>";

        String sanitized = ContractHtmlSanitizer.sanitize(raw);

        assertThat(sanitized).doesNotContain("<!DOCTYPE")
                .doesNotContain("<meta")
                .doesNotContain("<title")
                .doesNotContain("<html")
                .doesNotContain("<body");
        assertThat(sanitized).contains("표준근로계약서");
    }

    @Test
    @DisplayName("정상적인 HTML은 유지되어야 한다")
    void sanitize_ValidHtml_Preserved() {
        // Given
        String validHtml = "<p>안녕하세요</p><strong>계약서</strong><em>내용</em>";

        // When
        String result = ContractHtmlSanitizer.sanitize(validHtml);

        // Then
        assertThat(result).isEqualTo(validHtml);
    }

    @Test
    @DisplayName("서식 태그는 유지되어야 한다")
    void sanitize_FormattingTags_Preserved() {
        // Given
        String html = "<h1>제목</h1><h2>부제목</h2><p>단락</p><br/><ul><li>목록</li></ul>";

        // When
        String result = ContractHtmlSanitizer.sanitize(html);

        // Then
        assertThat(result).contains("<h1>", "<h2>", "<p>", "<br>", "<ul>", "<li>");
    }

    @Test
    @DisplayName("script 태그는 제거되어야 한다")
    void sanitize_ScriptTag_Removed() {
        // Given
        String maliciousHtml = "<p>정상 내용</p><script>alert('XSS')</script><p>끝</p>";

        // When
        String result = ContractHtmlSanitizer.sanitize(maliciousHtml);

        // Then
        assertThat(result).doesNotContain("<script>", "</script>", "alert('XSS')");
        assertThat(result).contains("<p>정상 내용</p>", "<p>끝</p>");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "onclick=\"alert('XSS')\"",
            "onerror=\"alert('XSS')\"",
            "onload=\"alert('XSS')\"",
            "onmouseover=\"alert('XSS')\"",
            "onfocus=\"alert('XSS')\"",
            "onblur=\"alert('XSS')\"",
            "onchange=\"alert('XSS')\"",
            "onsubmit=\"alert('XSS')\""
    })
    @DisplayName("모든 이벤트 핸들러는 제거되어야 한다")
    void sanitize_EventHandlers_Removed(String eventHandler) {
        // Given
        String html = "<div " + eventHandler + ">클릭해보세요</div>";

        // When
        String result = ContractHtmlSanitizer.sanitize(html);

        // Then
        assertThat(result).doesNotContain(eventHandler);
        assertThat(result).contains("클릭해보세요");
    }

    @Test
    @DisplayName("javascript: 프로토콜은 제거되어야 한다")
    void sanitize_JavascriptProtocol_Removed() {
        // Given
        String html = "<a href=\"javascript:alert('XSS')\">링크</a>";

        // When
        String result = ContractHtmlSanitizer.sanitize(html);

        // Then
        assertThat(result).doesNotContain("javascript:");
        assertThat(result).contains("<a>링크</a>");
    }

    @Test
    @DisplayName("인라인 스타일은 제거되어야 한다")
    void sanitize_InlineStyle_Removed() {
        // Given
        String html = "<div style=\"background:url('javascript:alert(\\'XSS\\')')\">스타일</div>";

        // When
        String result = ContractHtmlSanitizer.sanitize(html);

        // Then
        assertThat(result).doesNotContain("style=");
        assertThat(result).contains("스타일");
    }

    @Test
    @DisplayName("복잡한 XSS 공격은 방어되어야 한다")
    void sanitize_ComplexXSS_Prevented() {
        // Given
        String complexXss = "<div onclick=\"alert('XSS')\" onmouseover=\"alert('XSS')\">" +
                "<script>alert('XSS')</script>" +
                "<img src=\"x\" onerror=\"alert('XSS')\">" +
                "<a href=\"javascript:alert('XSS')\">링크</a>" +
                "</div>";

        // When
        String result = ContractHtmlSanitizer.sanitize(complexXss);

        // Then
        assertThat(result).doesNotContain(
                "onclick", "onmouseover", "onerror", "javascript:", "<script>"
        );
        assertThat(result).contains("<div>", "</div>");
    }

    @Test
    @DisplayName("UTF-8 한글은 유지되어야 한다")
    void sanitize_KoreanText_Preserved() {
        // Given
        String koreanHtml = "<p>안녕하세요. 계약서 내용입니다.</p><strong>중요한 조항</strong>";

        // When
        String result = ContractHtmlSanitizer.sanitize(koreanHtml);

        // Then
        assertThat(result).isEqualTo(koreanHtml);
        assertThat(result).contains("안녕하세요", "계약서", "중요한 조항");
    }

    @Test
    @DisplayName("HTML 엔티티는 유지되어야 한다")
    void sanitize_HtmlEntities_Preserved() {
        // Given
        String html = "<p>&lt;script&gt;는 위험합니다&lt;/script&gt;</p>";

        // When
        String result = ContractHtmlSanitizer.sanitize(html);

        // Then
        assertThat(result).contains("&lt;script&gt;", "&lt;/script&gt;");
    }

    @Test
    @DisplayName("빈 문자열은 빈 문자열로 반환되어야 한다")
    void sanitize_EmptyString_ReturnsEmpty() {
        // Given
        String emptyHtml = "";

        // When
        String result = ContractHtmlSanitizer.sanitize(emptyHtml);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("null은 빈 문자열로 반환되어야 한다")
    void sanitize_Null_ReturnsEmpty() {
        // Given
        String nullHtml = null;

        // When
        String result = ContractHtmlSanitizer.sanitize(nullHtml);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("텍스트만 있는 경우는 유지되어야 한다")
    void sanitize_TextOnly_Preserved() {
        // Given
        String textOnly = "이것은 순수 텍스트입니다. HTML 태그가 없습니다.";

        // When
        String result = ContractHtmlSanitizer.sanitize(textOnly);

        // Then
        assertThat(result).isEqualTo(textOnly);
    }

    @Test
    @DisplayName("중첩된 HTML 구조는 유지되어야 한다")
    void sanitize_NestedHtml_Preserved() {
        // Given
        String nestedHtml = "<div><p><strong><em>중첩된</em> 텍스트</strong></p></div>";

        // When
        String result = ContractHtmlSanitizer.sanitize(nestedHtml);

        // Then
        assertThat(result).contains("중첩된", "텍스트");
    }

    @Test
    @DisplayName("표 관련 태그는 유지되어야 한다")
    void sanitize_TableTags_Preserved() {
        // Given
        String tableHtml = "<table><tr><th>제목</th></tr><tr><td>내용</td></tr></table>";

        // When
        String result = ContractHtmlSanitizer.sanitize(tableHtml);

        // Then
        assertThat(result).contains("<table>", "<tr>", "<th>", "<td>", "</table>");
    }

    @Test
    @DisplayName("data 속성은 제거되어야 한다")
    void sanitize_DataAttributes_Removed() {
        // Given
        String html = "<div data-test=\"value\" data-user-id=\"123\">내용</div>";

        // When
        String result = ContractHtmlSanitizer.sanitize(html);

        // Then
        assertThat(result).doesNotContain("data-test", "data-user-id");
        assertThat(result).contains("내용");
    }
}
