package com.signly.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserIdTest {

    @Test
    @DisplayName("유효한 ULID 문자열로 UserId를 생성할 수 있다")
    void createUserId_WithValidUlid_ShouldSuccess() {
        String validUlid = "01ARZ3NDEKTSV4RRFFQ69G5FAV";

        UserId userId = UserId.of(validUlid);

        assertThat(userId.value()).isEqualTo(validUlid);
    }

    @Test
    @DisplayName("UserId를 자동 생성할 수 있다")
    void generateUserId_ShouldCreateValidUserId() {
        UserId userId = UserId.generate();

        assertThat(userId.value()).isNotNull();
        assertThat(userId.value()).hasSize(26);
    }

    @Test
    @DisplayName("null 값으로 UserId 생성 시 예외가 발생한다")
    void createUserId_WithNull_ShouldThrowException() {
        assertThatThrownBy(() -> UserId.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자 ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("빈 문자열로 UserId 생성 시 예외가 발생한다")
    void createUserId_WithEmptyString_ShouldThrowException() {
        assertThatThrownBy(() -> UserId.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자 ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("유효하지 않은 ULID 형식으로 생성 시 예외가 발생한다")
    void createUserId_WithInvalidUlid_ShouldThrowException() {
        assertThatThrownBy(() -> UserId.of("invalid-ulid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 사용자 ID 형식입니다");
    }

    @Test
    @DisplayName("같은 값의 UserId 객체는 동등하다")
    void equals_WithSameValue_ShouldBeEqual() {
        String ulid = "01ARZ3NDEKTSV4RRFFQ69G5FAV";
        UserId userId1 = UserId.of(ulid);
        UserId userId2 = UserId.of(ulid);

        assertThat(userId1).isEqualTo(userId2);
        assertThat(userId1.hashCode()).isEqualTo(userId2.hashCode());
    }

    @Test
    @DisplayName("다른 값의 UserId 객체는 동등하지 않다")
    void equals_WithDifferentValue_ShouldNotBeEqual() {
        UserId userId1 = UserId.generate();
        UserId userId2 = UserId.generate();

        assertThat(userId1).isNotEqualTo(userId2);
    }

    @Test
    @DisplayName("toString 메서드는 ULID 값을 반환한다")
    void toString_ShouldReturnUlidValue() {
        String ulid = "01ARZ3NDEKTSV4RRFFQ69G5FAV";
        UserId userId = UserId.of(ulid);

        String result = userId.toString();

        assertThat(result).isEqualTo(ulid);
    }
}