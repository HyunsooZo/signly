package com.signly.common.security;

import com.signly.user.domain.model.Email;
import com.signly.user.domain.model.User;
import com.signly.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 사용자 인증 정보 로드 (캐싱 적용)
     * 캐시 키: email
     * TTL: 15분 (보안상 짧게 설정)
     * <p>
     * 주의: 권한이나 계정 상태가 변경될 경우 캐시 무효화 필요
     */
    @Cacheable(value = "userDetails", key = "#email")
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(Email.of(email))
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        if (user.isLocked()) {
            log.warn("Login attempt for locked account: {}", email);
            throw new DisabledException("계정이 잠겨있습니다. 이메일을 확인해주세요.");
        }

        log.info("Loaded user details from DB: {} (cache miss)", email);

        // SecurityUser를 생성한 후 DTO로 변환하여 캐시
        SecurityUser securityUser = new SecurityUser(user);
        return UserDetailsDTO.from(securityUser);
    }
}