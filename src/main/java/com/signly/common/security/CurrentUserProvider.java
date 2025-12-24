package com.signly.common.security;

import com.signly.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CurrentUserProvider {

    public static final String DEMO_OWNER_ID = "01ARZ3NDEKTSV4RRFFQ69G5FAV";

    public String resolveUserId(
            UserPrincipal userPrincipal,
            HttpServletRequest request,
            String headerUserId,
            boolean required
    ) {

        if (userPrincipal != null && StringUtils.hasText(userPrincipal.getUserId())) {
            return userPrincipal.getUserId();
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

