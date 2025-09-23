package com.signly.contract.domain.model;

import com.signly.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ContractContentTest {

    @Test
    void 정상적인_내용으로_생성할_수_있다() {
        String content = "계약서 내용입니다.";

        ContractContent contractContent = ContractContent.of(content);

        assertThat(contractContent.getValue()).isEqualTo(content);
    }

    @Test
    void 앞뒤_공백이_제거된다() {
        String content = "  계약서 내용입니다.  ";

        ContractContent contractContent = ContractContent.of(content);

        assertThat(contractContent.getValue()).isEqualTo("계약서 내용입니다.");
    }

    @Test
    void null_내용으로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> ContractContent.of(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("계약서 내용은 필수입니다");
    }

    @Test
    void 빈_문자열로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> ContractContent.of(""))
                .isInstanceOf(ValidationException.class)
                .hasMessage("계약서 내용은 필수입니다");
    }

    @Test
    void 공백_문자열로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> ContractContent.of("   "))
                .isInstanceOf(ValidationException.class)
                .hasMessage("계약서 내용은 필수입니다");
    }

    @Test
    void 최대_길이를_초과하면_예외가_발생한다() {
        String longContent = "a".repeat(50001);

        assertThatThrownBy(() -> ContractContent.of(longContent))
                .isInstanceOf(ValidationException.class)
                .hasMessage("계약서 내용은 50,000자를 초과할 수 없습니다");
    }

    @Test
    void 최대_길이와_같으면_생성된다() {
        String maxContent = "a".repeat(50000);

        ContractContent contractContent = ContractContent.of(maxContent);

        assertThat(contractContent.getValue()).isEqualTo(maxContent);
        assertThat(contractContent.getLength()).isEqualTo(50000);
    }

    @Test
    void 길이를_반환한다() {
        String content = "계약서 내용";

        ContractContent contractContent = ContractContent.of(content);

        assertThat(contractContent.getLength()).isEqualTo(content.length());
    }

    @Test
    void 비어있는지_확인할_수_있다() {
        ContractContent contractContent = ContractContent.of("내용");

        assertThat(contractContent.isEmpty()).isFalse();
    }

    @Test
    void 같은_값을_가진_ContractContent는_동등하다() {
        String content = "계약서 내용";
        ContractContent content1 = ContractContent.of(content);
        ContractContent content2 = ContractContent.of(content);

        assertThat(content1).isEqualTo(content2);
        assertThat(content1.hashCode()).isEqualTo(content2.hashCode());
    }

    @Test
    void toString은_값을_반환한다() {
        String content = "계약서 내용";
        ContractContent contractContent = ContractContent.of(content);

        assertThat(contractContent.toString()).isEqualTo(content);
    }
}