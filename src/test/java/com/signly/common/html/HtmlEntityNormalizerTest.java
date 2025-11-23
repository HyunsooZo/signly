package com.signly.common.html;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HtmlEntityNormalizer 단위 테스트")
class HtmlEntityNormalizerTest {

    @Test
    @DisplayName("숫자 형식 entity 정규화 - 대괄호")
    void normalizeEntities_numericEntityBrackets() {
        String html = "&#91;EMPLOYER_SIGNATURE_IMAGE&#93;";

        String result = HtmlEntityNormalizer.normalizeEntities(html);

        assertThat(result).isEqualTo("[EMPLOYER_SIGNATURE_IMAGE]");
    }

    @Test
    @DisplayName("이중 인코딩 entity 정규화")
    void normalizeEntities_doubleEncodedEntity() {
        String html = "&amp;#91;EMPLOYER_SIGNATURE_IMAGE&amp;#93;";

        String result = HtmlEntityNormalizer.normalizeEntities(html);

        assertThat(result).isEqualTo("[EMPLOYER_SIGNATURE_IMAGE]");
    }

    @Test
    @DisplayName("Named entity 정규화")
    void normalizeEntities_namedEntity() {
        String html = "&lbrack;EMPLOYER_SIGNATURE_IMAGE&rbrack;";

        String result = HtmlEntityNormalizer.normalizeEntities(html);

        assertThat(result).isEqualTo("[EMPLOYER_SIGNATURE_IMAGE]");
    }

    @Test
    @DisplayName("중괄호 entity 정규화")
    void normalizeEntities_curlyBraces() {
        String html = "&#123;VARIABLE&#125;";

        String result = HtmlEntityNormalizer.normalizeEntities(html);

        assertThat(result).isEqualTo("{VARIABLE}");
    }

    @Test
    @DisplayName("혼합 entity 정규화")
    void normalizeEntities_mixedEntities() {
        String html = "&#91;NAME&#93; and &lbrack;DATE&rbrack; and &#123;VALUE&#125;";

        String result = HtmlEntityNormalizer.normalizeEntities(html);

        assertThat(result).isEqualTo("[NAME] and [DATE] and {VALUE}");
    }

    @Test
    @DisplayName("플레이스홀더 정규화")
    void normalizePlaceholders_employerSignature() {
        String html = "서명: &#91;EMPLOYER_SIGNATURE_IMAGE&#93;";

        String result = HtmlEntityNormalizer.normalizePlaceholders(html);

        assertThat(result).isEqualTo("서명: [EMPLOYER_SIGNATURE_IMAGE]");
    }

    @Test
    @DisplayName("플레이스홀더 정규화 - 근로자 서명")
    void normalizePlaceholders_employeeSignature() {
        String html = "서명: &lbrack;EMPLOYEE_SIGNATURE_IMAGE&rbrack;";

        String result = HtmlEntityNormalizer.normalizePlaceholders(html);

        assertThat(result).isEqualTo("서명: [EMPLOYEE_SIGNATURE_IMAGE]");
    }

    @Test
    @DisplayName("플레이스홀더 정규화 - 양측 서명")
    void normalizePlaceholders_bothSignatures() {
        String html = "갑: &#91;EMPLOYER_SIGNATURE_IMAGE&#93;, 을: &amp;#91;EMPLOYEE_SIGNATURE_IMAGE&amp;#93;";

        String result = HtmlEntityNormalizer.normalizePlaceholders(html);

        assertThat(result).isEqualTo("갑: [EMPLOYER_SIGNATURE_IMAGE], 을: [EMPLOYEE_SIGNATURE_IMAGE]");
    }

    @Test
    @DisplayName("빈 문자열 처리")
    void normalizeEntities_emptyString() {
        assertThat(HtmlEntityNormalizer.normalizeEntities("")).isEmpty();
        assertThat(HtmlEntityNormalizer.normalizeEntities(null)).isNull();
    }

    @Test
    @DisplayName("entity가 없는 HTML은 그대로 반환")
    void normalizeEntities_noEntities() {
        String html = "일반 텍스트입니다.";

        String result = HtmlEntityNormalizer.normalizeEntities(html);

        assertThat(result).isEqualTo(html);
    }
}
