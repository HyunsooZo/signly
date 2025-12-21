package com.signly.core.auth.oauth2;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserInfoExtractor {

    OAuth2UserInfo extract(OAuth2User oAuth2User) throws OAuth2AuthenticationException;

    String getProviderName();
}
