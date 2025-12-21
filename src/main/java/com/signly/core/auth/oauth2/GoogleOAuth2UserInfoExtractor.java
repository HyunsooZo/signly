package com.signly.core.auth.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component("google")
@Slf4j
public class GoogleOAuth2UserInfoExtractor implements OAuth2UserInfoExtractor {

    private static final String PROVIDER_NAME = "google";

    @Override
    public OAuth2UserInfo extract(OAuth2User oAuth2User) throws OAuth2AuthenticationException {
        log.debug("Extracting user info from Google OAuth2 response");

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");
        String sub = oAuth2User.getAttribute("sub");

        log.info("Google OAuth2 user info - email: {}, name: {}, verified: {}, sub: {}", email, name, emailVerified, sub);

        if (emailVerified == null || !emailVerified) {
            log.warn("Google email not verified for user: {}", email);
            throw new OAuth2AuthenticationException("Google 계정의 이메일 인증이 필요합니다");
        }

        return OAuth2UserInfo.builder()
                .email(email)
                .name(name)
                .emailVerified(true)
                .providerId(sub)
                .provider(PROVIDER_NAME)
                .build();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
}
