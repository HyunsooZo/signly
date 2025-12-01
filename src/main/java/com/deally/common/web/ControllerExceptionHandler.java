package com.deally.common.web;

import com.deally.common.exception.BusinessException;
import com.deally.common.exception.ValidationException;
import org.slf4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 컨트롤러 예외 처리 표준화 유틸리티
 * SRP: 컨트롤러 예외 처리 패턴 표준화 담당
 */
public class ControllerExceptionHandler {

    /**
     * 예외가 발생할 수 있는 작업을 안전하게 실행하고 예외를 표준화된 방식으로 처리
     *
     * @param operation     실행할 작업
     * @param logger        로거
     * @param operationName 작업 이름 (로깅용)
     * @param errorViewName 에러 시 반환할 뷰 이름
     * @param model         모델
     * @param errorMessage  사용자에게 표시할 에러 메시지
     * @return 성공 시 뷰 이름, 실패 시 errorViewName
     */
    public static String handleOperation(
            Supplier<String> operation,
            Logger logger,
            String operationName,
            String errorViewName,
            Model model,
            String errorMessage
    ) {
        try {
            return operation.get();
        } catch (ValidationException e) {
            return handleValidationException(e, logger, operationName, model, errorMessage, errorViewName);
        } catch (BusinessException e) {
            return handleBusinessException(e, logger, operationName, model, errorMessage, errorViewName);
        } catch (Exception e) {
            return handleGenericException(e, logger, operationName, model, errorMessage, errorViewName);
        }
    }

    /**
     * 리다이렉트 방식의 예외 처리
     */
    public static String handleOperationWithRedirect(
            Runnable operation,
            Logger logger,
            String operationName,
            String redirectUrl,
            RedirectAttributes redirectAttributes,
            String errorMessage
    ) {
        try {
            operation.run();
            return "redirect:" + redirectUrl;
        } catch (ValidationException e) {
            return handleValidationExceptionWithRedirect(e, logger, operationName, redirectAttributes, errorMessage);
        } catch (BusinessException e) {
            return handleBusinessExceptionWithRedirect(e, logger, operationName, redirectAttributes, errorMessage);
        } catch (Exception e) {
            return handleGenericExceptionWithRedirect(e, logger, operationName, redirectAttributes, errorMessage);
        }
    }

    /**
     * ValidationException 처리 (모델 방식)
     */
    private static String handleValidationException(
            ValidationException e,
            Logger logger,
            String operationName,
            Model model,
            String customMessage,
            String errorViewName
    ) {
        logger.warn("{} 유효성 검사 실패: {}", operationName, e.getMessage());
        model.addAttribute("errorMessage", customMessage != null ? customMessage : e.getMessage());
        return errorViewName != null ? errorViewName : determineErrorView(model);
    }

    /**
     * BusinessException 처리 (모델 방식)
     */
    private static String handleBusinessException(
            BusinessException e,
            Logger logger,
            String operationName,
            Model model,
            String customMessage,
            String errorViewName
    ) {
        logger.warn("{} 비즈니스 로직 실패: {}", operationName, e.getMessage());
        model.addAttribute("errorMessage", customMessage != null ? customMessage : e.getMessage());
        return errorViewName != null ? errorViewName : determineErrorView(model);
    }

    /**
     * 일반 예외 처리 (모델 방식)
     */
    private static String handleGenericException(
            Exception e,
            Logger logger,
            String operationName,
            Model model,
            String customMessage,
            String errorViewName
    ) {
        logger.error("{} 중 예상치 못한 오류 발생", operationName, e);
        model.addAttribute("errorMessage", customMessage != null ? customMessage : "처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return errorViewName != null ? errorViewName : determineErrorView(model);
    }

    /**
     * ValidationException 처리 (리다이렉트 방식)
     */
    private static String handleValidationExceptionWithRedirect(
            ValidationException e,
            Logger logger,
            String operationName,
            RedirectAttributes redirectAttributes,
            String customMessage
    ) {
        logger.warn("{} 유효성 검사 실패: {}", operationName, e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", customMessage != null ? customMessage : e.getMessage());
        return "redirect:/"; // 기본 리다이렉트
    }

    /**
     * BusinessException 처리 (리다이렉트 방식)
     */
    private static String handleBusinessExceptionWithRedirect(
            BusinessException e,
            Logger logger,
            String operationName,
            RedirectAttributes redirectAttributes,
            String customMessage
    ) {
        logger.warn("{} 비즈니스 로직 실패: {}", operationName, e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", customMessage != null ? customMessage : e.getMessage());
        return "redirect:/"; // 기본 리다이렉트
    }

    /**
     * 일반 예외 처리 (리다이렉트 방식)
     */
    private static String handleGenericExceptionWithRedirect(
            Exception e,
            Logger logger,
            String operationName,
            RedirectAttributes redirectAttributes,
            String customMessage
    ) {
        logger.error("{} 중 예상치 못한 오류 발생", operationName, e);
        redirectAttributes.addFlashAttribute("errorMessage", customMessage != null ? customMessage : "처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return "redirect:/"; // 기본 리다이렉트
    }

    /**
     * 모델에 설정된 뷰 이름을 결정 (fallback)
     */
    private static String determineErrorView(Model model) {
        // 모델에 이미 설정된 뷰 속성이 있으면 사용, 없으면 기본 에러 페이지
        if (model.containsAttribute("viewName")) {
            return Objects.requireNonNull(model.getAttribute("viewName")).toString();
        }
        return "error"; // 기본 에러 페이지
    }

    /**
     * 성공 메시지를 리다이렉트 속성에 추가
     */
    public static void addSuccessMessage(
            RedirectAttributes redirectAttributes,
            String message
    ) {
        redirectAttributes.addFlashAttribute("successMessage", message);
    }

    /**
     * 에러 메시지를 리다이렉트 속성에 추가
     */
    public static void addErrorMessage(
            RedirectAttributes redirectAttributes,
            String message
    ) {
        redirectAttributes.addFlashAttribute("errorMessage", message);
    }

    /**
     * 에러 메시지를 모델에 추가
     */
    public static void addErrorMessage(
            Model model,
            String message
    ) {
        model.addAttribute("errorMessage", message);
    }

    /**
     * 페이지 타이틀을 모델에 추가
     */
    public static void addPageTitle(
            Model model,
            String title
    ) {
        model.addAttribute("pageTitle", title);
    }
}