package com.signly.core.auth;

import com.signly.common.security.OAuth2SecurityUser;
import com.signly.core.auth.oauth2.OAuth2UserInfo;
import com.signly.core.auth.oauth2.OAuth2UserInfoExtractor;
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

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, OAuth2UserInfoExtractor> extractors;  // Spring이 자동 주입 (Bean 이름 = registrationId)

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 login attempt with provider: {}", registrationId);

        try {
            return processOAuth2User(oAuth2User, registrationId);
        } catch (OAuth2AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error processing OAuth2 user from provider: {}", registrationId, ex);
            throw new OAuth2AuthenticationException("OAuth2 사용자 처리 중 오류가 발생했습니다");
        }
    }

    private OAuth2User processOAuth2User(OAuth2User oAuth2User, String registrationId) {
        var extractor = extractors.get(registrationId);
        if (extractor == null) {
            log.error("Unsupported OAuth2 provider: {}", registrationId);
            throw new OAuth2AuthenticationException(
                    "지원하지 않는 OAuth2 프로바이더입니다: " + registrationId
            );
        }

        var userInfo = extractor.extract(oAuth2User);
        log.debug("Extracted OAuth2 user info: provider={}, email={}, name={}",
                userInfo.getProvider(), userInfo.getEmail(), userInfo.getName());

        if (userInfo.getEmail() == null || userInfo.getEmail().trim().isEmpty()) {
            log.error("Email is missing from OAuth2 user info: provider={}", registrationId);
            throw new OAuth2AuthenticationException("이메일 정보를 가져올 수 없습니다");
        }

        final String userName = (userInfo.getName() == null || userInfo.getName().trim().isEmpty())
                ? userInfo.getEmail().split("@")[0]
                : userInfo.getName();

        Email userEmail = Email.of(userInfo.getEmail());
        User user = userRepository.findByEmail(userEmail)
                .orElseGet(() -> createNewOAuth2User(userEmail, userName, userInfo.getProvider()));

        validateAndActivateUser(user, userInfo.getEmail());

        String nameAttributeKey = "google".equals(registrationId) ? "sub" : "response";
        return new OAuth2SecurityUser(user, oAuth2User.getAttributes(), nameAttributeKey);
    }

    private void validateAndActivateUser(User user, String email) {
        if (user.getStatus() == UserStatus.SUSPENDED) {
            log.warn("Login attempt by suspended user: {}", email);
            throw new OAuth2AuthenticationException("정지된 계정입니다. 관리자에게 문의하세요.");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            log.warn("Login attempt by inactive user: {}", email);
            throw new OAuth2AuthenticationException("비활성화된 계정입니다");
        }

        if (user.getStatus() == UserStatus.PENDING) {
            user.activate();
            userRepository.save(user);
            log.info("OAuth2 user activated: {}", email);
        }
    }

    private User createNewOAuth2User(Email email, String name, String provider) {
        log.info("Creating new OAuth2 user: email={}, provider={}", email.value(), provider);

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

        newUser.activate(); // PENDING → ACTIVE

        User savedUser = userRepository.save(newUser);
        log.info("New OAuth2 user created successfully: email={}, userId={}", email.value(), savedUser.getUserId());

        return savedUser;
    }
}
