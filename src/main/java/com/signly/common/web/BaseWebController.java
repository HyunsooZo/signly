package com.signly.common.web;

import com.signly.common.exception.BusinessException;
import com.signly.common.exception.UnauthorizedException;
import com.signly.common.exception.ValidationException;
import com.signly.common.security.CurrentUserProvider;
import com.signly.common.security.SecurityUser;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 베이스 웹 컨트롤러
 * SRP: 공통 웹 컨트롤러 기능 제공
 */
public abstract class BaseWebController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

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

    /**
     * 현재 사용자 ID를 안전하게 해석
     */
    protected String resolveUserId(
            CurrentUserProvider currentUserProvider,
            SecurityUser securityUser,
            HttpServletRequest request,
            String userId,
            boolean required
    ) {
        return currentUserProvider.resolveUserId(securityUser, request, userId, required);
    }

    /**
     * 예외가 발생할 수 있는 작업을 표준화된 방식으로 처리
     */
    protected String handleOperation(
            Supplier<String> operation,
            String operationName,
            String errorViewName,
            Model model,
            String errorMessage
    ) {
        return ControllerExceptionHandler.handleOperation(
                operation, logger, operationName, errorViewName, model, errorMessage);
    }

    /**
     * 리다이렉트 방식의 예외 처리
     */
    protected String handleOperationWithRedirect(
            Runnable operation,
            String operationName,
            String redirectUrl,
            RedirectAttributes redirectAttributes,
            String errorMessage
    ) {
        return ControllerExceptionHandler.handleOperationWithRedirect(
                operation, logger, operationName, redirectUrl, redirectAttributes, errorMessage);
    }

    /**
     * 성공 메시지 추가
     */
    protected void addSuccessMessage(
            RedirectAttributes redirectAttributes,
            String message
    ) {
        ControllerExceptionHandler.addSuccessMessage(redirectAttributes, message);
    }

    /**
     * 에러 메시지 추가
     */
    protected void addErrorMessage(
            RedirectAttributes redirectAttributes,
            String message
    ) {
        ControllerExceptionHandler.addErrorMessage(redirectAttributes, message);
    }

    protected void addErrorMessage(
            Model model,
            String message
    ) {
        ControllerExceptionHandler.addErrorMessage(model, message);
    }

    /**
     * 페이지 타이틀 추가
     */
    protected void addPageTitle(
            Model model,
            String title
    ) {
        ControllerExceptionHandler.addPageTitle(model, title);
    }

    /**
     * ValidationException 처리를 위한 간편 메소드
     */
    protected String handleValidationException(
            ValidationException e,
            Model model,
            String customMessage
    ) {
        logger.warn("유효성 검사 실패: {}", e.getMessage());
        addErrorMessage(model, customMessage != null ? customMessage : e.getMessage());
        return determineErrorView(model);
    }

    /**
     * BusinessException 처리를 위한 간편 메소드
     */
    protected String handleBusinessException(
            BusinessException e,
            Model model,
            String customMessage
    ) {
        logger.warn("비즈니스 로직 실패: {}", e.getMessage());
        addErrorMessage(model, customMessage != null ? customMessage : e.getMessage());
        return determineErrorView(model);
    }

    /**
     * 일반 예외 처리를 위한 간편 메소드
     */
    protected String handleGenericException(
            Exception e,
            Model model,
            String operationName,
            String customMessage
    ) {
        logger.error("{} 중 예상치 못한 오류 발생", operationName, e);
        addErrorMessage(model, customMessage != null ? customMessage : "처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return determineErrorView(model);
    }

    /**
     * 에러 뷰 이름 결정
     */
    private String determineErrorView(Model model) {
        if (model.containsAttribute("viewName")) {
            return Objects.requireNonNull(model.getAttribute("viewName")).toString();
        }
        return "error";
    }
}
