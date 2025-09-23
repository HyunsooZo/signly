package com.signly.contract.domain.model;

import com.signly.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PartyInfoTest {

    @Test
    void 정상적인_정보로_생성할_수_있다() {
        String name = "홍길동";
        String email = "test@example.com";
        String organization = "회사명";

        PartyInfo partyInfo = PartyInfo.of(name, email, organization);

        assertThat(partyInfo.getName()).isEqualTo(name);
        assertThat(partyInfo.getEmail()).isEqualTo(email.toLowerCase());
        assertThat(partyInfo.getOrganizationName()).isEqualTo(organization);
    }

    @Test
    void 조직명_없이_생성할_수_있다() {
        String name = "홍길동";
        String email = "test@example.com";

        PartyInfo partyInfo = PartyInfo.of(name, email, null);

        assertThat(partyInfo.getName()).isEqualTo(name);
        assertThat(partyInfo.getEmail()).isEqualTo(email.toLowerCase());
        assertThat(partyInfo.getOrganizationName()).isNull();
        assertThat(partyInfo.hasOrganization()).isFalse();
    }

    @Test
    void 이메일이_소문자로_변환된다() {
        String email = "TEST@EXAMPLE.COM";

        PartyInfo partyInfo = PartyInfo.of("홍길동", email, "회사명");

        assertThat(partyInfo.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void 앞뒤_공백이_제거된다() {
        String name = "  홍길동  ";
        String email = "  test@example.com  ";
        String organization = "  회사명  ";

        PartyInfo partyInfo = PartyInfo.of(name, email, organization);

        assertThat(partyInfo.getName()).isEqualTo("홍길동");
        assertThat(partyInfo.getEmail()).isEqualTo("test@example.com");
        assertThat(partyInfo.getOrganizationName()).isEqualTo("회사명");
    }

    @Test
    void null_이름으로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> PartyInfo.of(null, "test@example.com", "회사명"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("당사자 이름은 필수입니다");
    }

    @Test
    void 빈_이름으로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> PartyInfo.of("", "test@example.com", "회사명"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("당사자 이름은 필수입니다");
    }

    @Test
    void 이름이_너무_길면_예외가_발생한다() {
        String longName = "a".repeat(101);

        assertThatThrownBy(() -> PartyInfo.of(longName, "test@example.com", "회사명"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("당사자 이름은 100자를 초과할 수 없습니다");
    }

    @Test
    void null_이메일로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> PartyInfo.of("홍길동", null, "회사명"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("당사자 이메일은 필수입니다");
    }

    @Test
    void 잘못된_이메일_형식으로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> PartyInfo.of("홍길동", "invalid-email", "회사명"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("올바른 이메일 형식이 아닙니다");
    }

    @Test
    void 조직명이_너무_길면_예외가_발생한다() {
        String longOrganization = "a".repeat(201);

        assertThatThrownBy(() -> PartyInfo.of("홍길동", "test@example.com", longOrganization))
                .isInstanceOf(ValidationException.class)
                .hasMessage("조직명은 200자를 초과할 수 없습니다");
    }

    @Test
    void 조직이_있는지_확인할_수_있다() {
        PartyInfo withOrganization = PartyInfo.of("홍길동", "test@example.com", "회사명");
        PartyInfo withoutOrganization = PartyInfo.of("홍길동", "test@example.com", null);
        PartyInfo withEmptyOrganization = PartyInfo.of("홍길동", "test@example.com", "");

        assertThat(withOrganization.hasOrganization()).isTrue();
        assertThat(withoutOrganization.hasOrganization()).isFalse();
        assertThat(withEmptyOrganization.hasOrganization()).isFalse();
    }

    @Test
    void 같은_값을_가진_PartyInfo는_동등하다() {
        PartyInfo party1 = PartyInfo.of("홍길동", "test@example.com", "회사명");
        PartyInfo party2 = PartyInfo.of("홍길동", "test@example.com", "회사명");

        assertThat(party1).isEqualTo(party2);
        assertThat(party1.hashCode()).isEqualTo(party2.hashCode());
    }

    @Test
    void 다른_값을_가진_PartyInfo는_동등하지_않다() {
        PartyInfo party1 = PartyInfo.of("홍길동", "test@example.com", "회사명");
        PartyInfo party2 = PartyInfo.of("김철수", "test@example.com", "회사명");

        assertThat(party1).isNotEqualTo(party2);
    }
}