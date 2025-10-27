package com.signly.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackages = "com.signly.*.presentation.web")
public class WebExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebExceptionHandler.class);

    @ExceptionHandler(UnauthorizedException.class)
    public ModelAndView handleUnauthorizedException(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {
        logger.warn("Unauthorized access to web page: {}", ex.getMessage());

        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        String returnUrl = queryString != null ? requestUri + "?" + queryString : requestUri;

        ModelAndView mav = new ModelAndView("redirect:/login");
        mav.addObject("returnUrl", returnUrl);
        mav.addObject("error", "세션이 만료되었습니다. 다시 로그인해주세요.");

        return mav;
    }

    @ExceptionHandler(ForbiddenException.class)
    public ModelAndView handleForbiddenException(
            ForbiddenException ex,
            HttpServletRequest request
    ) {
        logger.warn("Forbidden access to web page: {}", ex.getMessage());

        ModelAndView mav = new ModelAndView("error/403");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("requestUri", request.getRequestURI());

        return mav;
    }

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView handleNotFoundException(
            NotFoundException ex,
            HttpServletRequest request
    ) {
        logger.warn("Resource not found on web page: {}", ex.getMessage());

        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("requestUri", request.getRequestURI());

        return mav;
    }

    @ExceptionHandler(BusinessException.class)
    public ModelAndView handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        logger.warn("Business error on web page: {}", ex.getMessage());

        String referer = request.getHeader("Referer");
        ModelAndView mav = new ModelAndView(referer != null ? "redirect:" + referer : "redirect:/");
        mav.addObject("error", ex.getMessage());

        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        logger.error("Unexpected error on web page", ex);

        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("errorMessage", "서버 내부 오류가 발생했습니다.");
        mav.addObject("requestUri", request.getRequestURI());

        return mav;
    }
}
