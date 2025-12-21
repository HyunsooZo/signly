package com.signly.core.auth.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("naver")
@Slf4j
public class NaverOAuth2UserInfoExtractor implements OAuth2UserInfoExtractor {

    private static final String PROVIDER_NAME = "naver";

    @Override
    public OAuth2UserInfo extract(OAuth2User oAuth2User) throws OAuth2AuthenticationException {
        log.debug("Extracting user info from Naver OAuth2 response");

        Map<String, Object> response = oAuth2User.getAttribute("response");

        if (response == null) {
            log.error("Naver OAuth2 response object is null");
            throw new OAuth2AuthenticationException("네이버 사용자 정보를 가져올 수 없습니다");
        }

        String email = (String) response.get("email");
        String name = (String) response.get("name");
        String id = (String) response.get("id");

        log.info("Naver OAuth2 user info - email: {}, name: {}, id: {}", email, name, id);

        return OAuth2UserInfo.builder()
                .email(email)
                .name(name)
                .emailVerified(true)
                .providerId(id)
                .provider(PROVIDER_NAME)
                .build();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
}
