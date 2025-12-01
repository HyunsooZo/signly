package com.deally.contract.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ContractVariablePattern 단위 테스트")
class ContractVariablePatternTest {

    @Test
    @DisplayName("중괄호 패턴 변수 추출")
    void extractVariables_curlyBracePattern() {
        String html = "계약서에 {EMPLOYER_NAME}과 {EMPLOYEE_NAME}이 서명합니다.";

        List<String> variables = ContractVariablePattern.extractVariables(html);

        assertThat(variables).containsExactly("EMPLOYER_NAME", "EMPLOYEE_NAME");
    }

    @Test
    @DisplayName("대괄호 패턴 변수 추출")
    void extractVariables_squareBracketPattern() {
        String html = "계약서에 [사업주]와 [근로자]가 서명합니다.";

        List<String> variables = ContractVariablePattern.extractVariables(html);

        assertThat(variables).containsExactly("사업주", "근로자");
    }

    @Test
    @DisplayName("혼합 패턴 변수 추출")
    void extractVariables_mixedPattern() {
        String html = "{EMPLOYER_NAME}과 [근로자]가 {CONTRACT_DATE}에 계약합니다.";

        List<String> variables = ContractVariablePattern.extractVariables(html);

        assertThat(variables).containsExactly("EMPLOYER_NAME", "근로자", "CONTRACT_DATE");
    }

    @Test
    @DisplayName("서명 이미지 플레이스홀더는 제외")
    void extractVariables_ignoreSignaturePlaceholders() {
        String html = "{EMPLOYER_NAME}이 [EMPLOYER_SIGNATURE_IMAGE]에 서명합니다.";

        List<String> variables = ContractVariablePattern.extractVariables(html);

        assertThat(variables).containsExactly("EMPLOYER_NAME");
        assertThat(variables).doesNotContain("EMPLOYER_SIGNATURE_IMAGE");
    }

    @Test
    @DisplayName("중복 변수는 한 번만 추출")
    void extractVariables_duplicatesRemovedWithOrder() {
        String html = "{NAME}과 {DATE}와 {NAME}이 있습니다.";

        List<String> variables = ContractVariablePattern.extractVariables(html);

        assertThat(variables).containsExactly("NAME", "DATE");
    }

    @Test
    @DisplayName("빈 HTML은 빈 리스트 반환")
    void extractVariables_emptyHtml() {
        assertThat(ContractVariablePattern.extractVariables("")).isEmpty();
        assertThat(ContractVariablePattern.extractVariables(null)).isEmpty();
    }

    @Test
    @DisplayName("변수가 없는 HTML은 빈 리스트 반환")
    void extractVariables_noVariables() {
        String html = "변수가 없는 일반 텍스트입니다.";

        List<String> variables = ContractVariablePattern.extractVariables(html);

        assertThat(variables).isEmpty();
    }

    @Test
    @DisplayName("공백이 포함된 변수도 추출 (trim 적용)")
    void extractVariables_variablesWithWhitespace() {
        String html = "{ EMPLOYER_NAME }과 [ 근로자 ]가 계약합니다.";

        List<String> variables = ContractVariablePattern.extractVariables(html);

        assertThat(variables).containsExactly("EMPLOYER_NAME", "근로자");
    }

    @Test
    @DisplayName("무시 변수 확인")
    void isIgnored() {
        assertThat(ContractVariablePattern.isIgnored("EMPLOYER_SIGNATURE_IMAGE")).isTrue();
        assertThat(ContractVariablePattern.isIgnored("EMPLOYEE_SIGNATURE_IMAGE")).isTrue();
        assertThat(ContractVariablePattern.isIgnored("EMPLOYER_NAME")).isFalse();
    }

    @Test
    @DisplayName("HTML에 특정 변수 포함 여부 확인 - 중괄호")
    void containsVariable_curlyBrace() {
        String html = "계약서에 {EMPLOYER_NAME}이 있습니다.";

        assertThat(ContractVariablePattern.containsVariable(html, "EMPLOYER_NAME")).isTrue();
        assertThat(ContractVariablePattern.containsVariable(html, "EMPLOYEE_NAME")).isFalse();
    }

    @Test
    @DisplayName("HTML에 특정 변수 포함 여부 확인 - 대괄호")
    void containsVariable_squareBracket() {
        String html = "계약서에 [사업주]가 있습니다.";

        assertThat(ContractVariablePattern.containsVariable(html, "사업주")).isTrue();
        assertThat(ContractVariablePattern.containsVariable(html, "근로자")).isFalse();
    }
}
