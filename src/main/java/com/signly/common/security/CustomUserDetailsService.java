package com.signly.common.security;

import com.signly.user.domain.model.Email;
import com.signly.user.domain.model.User;
import com.signly.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 사용자 인증 정보 로드
     * 캐싱 제거: 사용자 정보는 자주 변경되고 민감하므로 캐싱하지 않음
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(Email.of(email))
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        if (user.isLocked()) {
            log.warn("Login attempt for locked account: {}", email);
            throw new DisabledException("계정이 잠겨있습니다. 이메일을 확인해주세요.");
        }

        log.debug("Loaded user details from DB: {}", email);

        // SecurityUser를 생성하여 반환
        return new SecurityUser(user);
    }
}