package com.signly.common.security;

import com.signly.user.domain.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * OAuth2 로그인 사용자 정보
 * - UserDetails와 OAuth2User를 모두 구현
 * - SecurityUser를 확장하여 기존 기능 유지
 */
@Getter
public class OAuth2SecurityUser extends SecurityUser implements OAuth2User {

    private final Map<String, Object> attributes;
    private final String nameAttributeKey;

    /**
     * OAuth2SecurityUser 생성자
     *
     * @param user              도메인 User 객체
     * @param attributes        OAuth2 공급자로부터 받은 사용자 속성
     * @param nameAttributeKey  사용자 이름 속성의 키 (예: Google은 "sub")
     */
    public OAuth2SecurityUser(User user, Map<String, Object> attributes, String nameAttributeKey) {
        super(user);
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
    }

    // ===== OAuth2User 인터페이스 구현 =====

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // SecurityUser의 getAuthorities() 메서드를 그대로 사용
        return super.getAuthorities();
    }

    @Override
    public String getName() {
        // OAuth2User의 getName()은 nameAttributeKey에 해당하는 값을 반환
        // Google의 경우 "sub" (subject identifier)
        if (attributes != null && nameAttributeKey != null && attributes.containsKey(nameAttributeKey)) {
            return String.valueOf(attributes.get(nameAttributeKey));
        }
        // fallback: User의 이름 반환
        return super.getName();
    }

    // ===== UserDetails 인터페이스 구현 =====
    // SecurityUser에서 상속받은 메서드들을 그대로 사용:
    // - getPassword()
    // - getUsername() (이메일)
    // - isAccountNonExpired()
    // - isAccountNonLocked()
    // - isCredentialsNonExpired()
    // - isEnabled()

    /**
     * 도메인 User 객체 반환 (편의 메서드)
     */
    @Override
    public User getUser() {
        return super.getUser();
    }
}
