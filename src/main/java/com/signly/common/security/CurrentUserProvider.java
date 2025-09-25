package com.signly.common.security;

import com.signly.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CurrentUserProvider {

    public static final String DEMO_OWNER_ID = "dbd51de0-b234-47d8-893b-241c744e7337";

    public String resolveUserId(SecurityUser securityUser,
                                HttpServletRequest request,
                                String headerUserId,
                                boolean required) {

        if (securityUser != null && StringUtils.hasText(securityUser.getUserId())) {
            return securityUser.getUserId();
        }

        if (request != null) {
            Object userIdAttr = request.getAttribute("userId");
            if (userIdAttr instanceof String userId && StringUtils.hasText(userId)) {
                return userId;
            }
        }

        if (StringUtils.hasText(headerUserId)) {
            return headerUserId;
        }

        if (!required) {
            return DEMO_OWNER_ID;
        }

        throw new UnauthorizedException("사용자 정보를 확인할 수 없습니다.");
    }
}

