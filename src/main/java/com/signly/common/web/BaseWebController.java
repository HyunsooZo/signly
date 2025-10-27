package com.signly.common.web;

import com.signly.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

public abstract class BaseWebController {

    @ExceptionHandler(UnauthorizedException.class)
    public ModelAndView handleUnauthorizedException(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        String returnUrl = queryString != null ? requestUri + "?" + queryString : requestUri;

        ModelAndView mav = new ModelAndView("redirect:/login");
        mav.addObject("returnUrl", returnUrl);
        mav.addObject("error", "세션이 만료되었습니다. 다시 로그인해주세요.");

        return mav;
    }
}
