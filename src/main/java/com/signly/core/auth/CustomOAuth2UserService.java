package com.signly.core.auth;

import com.signly.common.security.OAuth2SecurityUser;
import com.signly.user.domain.model.*;
import com.signly.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Google OAuth2 로그인 사용자 정보 처리 서비스
 * - Google에서 받은 사용자 정보를 기반으로 자동 회원가입 또는 로그인 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(oAuth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException("OAuth2 사용자 처리 중 오류가 발생했습니다");
        }
    }

    /**
     * OAuth2 사용자 정보 처리
     * - 기존 사용자: 로그인
     * - 신규 사용자: 자동 회원가입 (ACTIVE 상태로, 이메일 인증 완료로)
     */
    private OAuth2User processOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");

        if (email == null || email.trim().isEmpty()) {
            throw new OAuth2AuthenticationException("이메일 정보를 가져올 수 없습니다");
        }

        // 이름이 없으면 이메일 아이디 사용 (final 변수로 생성)
        final String userName = (name == null || name.trim().isEmpty())
                ? email.split("@")[0]
                : name;

        // Google에서 이메일 인증이 완료되지 않은 경우 차단
        if (emailVerified == null || !emailVerified) {
            log.warn("Google email not verified for user: {}", email);
            throw new OAuth2AuthenticationException("Google 계정의 이메일 인증이 필요합니다");
        }

        Email userEmail = Email.of(email);
        User user = userRepository.findByEmail(userEmail)
                .orElseGet(() -> createNewOAuth2User(userEmail, userName));

        // 사용자 상태 확인
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new OAuth2AuthenticationException("정지된 계정입니다. 관리자에게 문의하세요.");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new OAuth2AuthenticationException("비활성화된 계정입니다");
        }

        // OAuth2로 로그인한 사용자가 PENDING 상태라면 자동으로 ACTIVE로 전환
        // (Google 이메일은 이미 인증되었으므로)
        if (user.getStatus() == UserStatus.PENDING) {
            user.activate();
            userRepository.save(user);
            log.info("OAuth2 user activated: {}", email);
        }

        // OAuth2SecurityUser 반환 (UserDetails + OAuth2User 구현)
        return new OAuth2SecurityUser(user, oAuth2User.getAttributes(), "sub");
    }

    /**
     * 신규 OAuth2 사용자 생성
     * - 이메일 인증 완료 상태로 생성 (Google 인증 완료)
     * - ACTIVE 상태로 생성
     * - 비밀번호는 랜덤 UUID (OAuth2만 사용)
     */
    private User createNewOAuth2User(
            Email email,
            String name
    ) {
        log.info("Creating new OAuth2 user: {}", email.value());

        // OAuth2 사용자는 비밀번호 로그인 불가 (정책 만족하는 랜덤 비밀번호)
        String oauthPassword = "OAuth2User!" + UUID.randomUUID().toString().replace("-", "");
        Password randomPassword = Password.of(oauthPassword);

        User newUser = User.create(
                email,
                randomPassword,
                name,
                Company.empty(),
                UserType.OWNER, // 기본값: OWNER
                passwordEncoder
        );

        // OAuth2 사용자는 이메일 인증 완료 상태로 생성
        newUser.activate(); // PENDING → ACTIVE

        return userRepository.save(newUser);
    }
}
