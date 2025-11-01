package com.signly.user.domain.model;

import com.signly.common.exception.ValidationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

/**
 * 인코딩된 비밀번호를 나타내는 값 객체
 * 도메인 모델의 캡슐화를 위해 비밀번호 인코딩을 책임짐
 */
public class EncodedPassword {
    
    private final String value;
    
    private EncodedPassword(String value) {
        this.value = value;
    }
    
    /**
     * 평문 비밀번호를 인코딩하여 EncodedPassword 객체 생성
     * 
     * @param password 평문 비밀번호
     * @param passwordEncoder 비밀번호 인코더
     * @return 인코딩된 비밀번호 객체
     * @throws ValidationException 비밀번호가 null이거나 빈 경우
     */
    public static EncodedPassword from(Password password, PasswordEncoder passwordEncoder) {
        if (password == null) {
            throw new ValidationException("비밀번호는 필수입니다");
        }
        
        String encodedValue = passwordEncoder.encode(password.getValue());
        return new EncodedPassword(encodedValue);
    }
    
    /**
     * 이미 인코딩된 비밀번호 값으로부터 EncodedPassword 객체 생성
     * 주로 데이터베이스에서 복원할 때 사용
     * 
     * @param encodedValue 인코딩된 비밀번호 값
     * @return EncodedPassword 객체
     * @throws ValidationException 인코딩된 값이 null이거나 빈 경우
     */
    public static EncodedPassword of(String encodedValue) {
        if (encodedValue == null || encodedValue.trim().isEmpty()) {
            throw new ValidationException("인코딩된 비밀번호는 필수입니다");
        }
        return new EncodedPassword(encodedValue);
    }
    
    /**
     * 평문 비밀번호가 이 인코딩된 비밀번호와 일치하는지 검증
     * 
     * @param password 검증할 평문 비밀번호
     * @param passwordEncoder 비밀번호 인코더
     * @return 일치하면 true, 아니면 false
     */
    public boolean matches(Password password, PasswordEncoder passwordEncoder) {
        if (password == null || passwordEncoder == null) {
            return false;
        }
        return passwordEncoder.matches(password.getValue(), this.value);
    }
    
    /**
     * 인코딩된 비밀번호 값을 반환
     * 내부 사용을 위한 메서드로, 외부에서 직접 사용을 최소화해야 함
     * 
     * @return 인코딩된 비밀번호 값
     */
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedPassword that = (EncodedPassword) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        // 보안을 위해 실제 값 노출 방지
        return "EncodedPassword[***]";
    }
}