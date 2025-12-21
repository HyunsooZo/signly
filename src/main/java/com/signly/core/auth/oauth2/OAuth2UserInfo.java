package com.signly.core.auth.oauth2;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuth2UserInfo {

    private final String email;
    private final String name;
    private final boolean emailVerified;
    private final String providerId;
    private final String provider;
}
