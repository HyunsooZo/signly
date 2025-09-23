package com.signly.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class UserIdTest {

    @Test
    @DisplayName("유효한 UUID 문자열로 UserId를 생성할 수 있다")
    void createUserId_WithValidUuid_ShouldSuccess() {
        String validUuid = UUID.randomUUID().toString();

        UserId userId = UserId.of(validUuid);

        assertThat(userId.getValue()).isEqualTo(validUuid);
    }

    @Test
    @DisplayName("UserId를 자동 생성할 수 있다")
    void generateUserId_ShouldCreateValidUserId() {
        UserId userId = UserId.generate();

        assertThat(userId.getValue()).isNotNull();
        assertThat(UUID.fromString(userId.getValue())).isNotNull();
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
    @DisplayName("유효하지 않은 UUID 형식으로 생성 시 예외가 발생한다")
    void createUserId_WithInvalidUuid_ShouldThrowException() {
        assertThatThrownBy(() -> UserId.of("invalid-uuid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 사용자 ID 형식입니다");
    }

    @Test
    @DisplayName("같은 값의 UserId 객체는 동등하다")
    void equals_WithSameValue_ShouldBeEqual() {
        String uuid = UUID.randomUUID().toString();
        UserId userId1 = UserId.of(uuid);
        UserId userId2 = UserId.of(uuid);

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
    @DisplayName("toString 메서드는 UUID 값을 반환한다")
    void toString_ShouldReturnUuidValue() {
        String uuid = UUID.randomUUID().toString();
        UserId userId = UserId.of(uuid);

        String result = userId.toString();

        assertThat(result).isEqualTo(uuid);
    }
}