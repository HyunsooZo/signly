package com.signly.domain.contract.model;

import com.signly.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ContractIdTest {

    @Test
    void 정상적인_ID로_생성할_수_있다() {
        String validId = "550e8400-e29b-41d4-a716-446655440000";

        ContractId contractId = ContractId.of(validId);

        assertThat(contractId.getValue()).isEqualTo(validId);
    }

    @Test
    void 자동_생성할_수_있다() {
        ContractId contractId = ContractId.generate();

        assertThat(contractId.getValue()).isNotNull();
        assertThat(contractId.getValue()).isNotEmpty();
    }

    @Test
    void null_ID로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> ContractId.of(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("계약서 ID는 필수입니다");
    }

    @Test
    void 빈_문자열_ID로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> ContractId.of(""))
                .isInstanceOf(ValidationException.class)
                .hasMessage("계약서 ID는 필수입니다");
    }

    @Test
    void 공백_문자열_ID로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> ContractId.of("   "))
                .isInstanceOf(ValidationException.class)
                .hasMessage("계약서 ID는 필수입니다");
    }

    @Test
    void 같은_값을_가진_ContractId는_동등하다() {
        String id = "550e8400-e29b-41d4-a716-446655440000";
        ContractId contractId1 = ContractId.of(id);
        ContractId contractId2 = ContractId.of(id);

        assertThat(contractId1).isEqualTo(contractId2);
        assertThat(contractId1.hashCode()).isEqualTo(contractId2.hashCode());
    }

    @Test
    void 다른_값을_가진_ContractId는_동등하지_않다() {
        ContractId contractId1 = ContractId.of("550e8400-e29b-41d4-a716-446655440000");
        ContractId contractId2 = ContractId.of("550e8400-e29b-41d4-a716-446655440001");

        assertThat(contractId1).isNotEqualTo(contractId2);
    }

    @Test
    void toString은_값을_반환한다() {
        String id = "550e8400-e29b-41d4-a716-446655440000";
        ContractId contractId = ContractId.of(id);

        assertThat(contractId.toString()).isEqualTo(id);
    }
}