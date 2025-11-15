package com.signly.user.domain.model;

import com.signly.common.exception.ValidationException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 인코딩된 비밀번호를 나타내는 값 객체
 * 도메인 모델의 캡슐화를 위해 비밀번호 인코딩을 책임짐
 */
public record EncodedPassword(String value) {

    /**
     * 평문 비밀번호를 인코딩하여 EncodedPassword 객체 생성
     */
    public static EncodedPassword from(
            Password password,
            PasswordEncoder passwordEncoder
    ) {
        if (password == null) {
            throw new ValidationException("비밀번호는 필수입니다");
        }

        String encodedValue = passwordEncoder.encode(password.value());
        return new EncodedPassword(encodedValue);
    }

    /**
     * 이미 인코딩된 비밀번호 값으로부터 EncodedPassword 객체 생성
     * 주로 데이터베이스에서 복원할 때 사용
     */
    public static EncodedPassword of(String encodedValue) {
        if (encodedValue == null || encodedValue.trim().isEmpty()) {
            throw new ValidationException("인코딩된 비밀번호는 필수입니다");
        }
        return new EncodedPassword(encodedValue);
    }

    /**
     * 평문 비밀번호가 이 인코딩된 비밀번호와 일치하는지 검증
     */
    public boolean matches(
            Password password,
            PasswordEncoder passwordEncoder
    ) {
        if (password == null || passwordEncoder == null) {
            return false;
        }
        return passwordEncoder.matches(password.value(), this.value);
    }

    @Override
    public String toString() {
        // 보안을 위해 실제 값 노출 방지
        return "EncodedPassword[***]";
    }

}