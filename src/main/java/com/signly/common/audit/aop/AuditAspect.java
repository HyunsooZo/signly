package com.signly.common.audit.aop;

import com.signly.common.audit.application.AuditLogService;
import com.signly.common.audit.application.dto.CreateAuditLogCommand;
import com.signly.common.audit.domain.model.AuditDetails;
import com.signly.user.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.signly.common.security.SecurityUser;

import java.util.HashMap;
import java.util.Map;

/**
 * 감사 로그 자동 생성 AOP Aspect
 *
 * @Auditable 애노테이션이 붙은 메서드 실행 후 자동으로 감사 로그를 생성
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(auditable)")
    public Object audit(
            ProceedingJoinPoint joinPoint,
            Auditable auditable
    ) throws Throwable {
        // 메서드 실행 전 시간 기록
        long startTime = System.currentTimeMillis();

        // 메서드 실행
        Object result = joinPoint.proceed();

        // 메서드 실행 후 감사 로그 생성
        try {
            String entityId = extractEntityId(joinPoint, result, auditable);
            String userId = extractUserId(joinPoint);
            String ipAddress = AuditContextHolder.getIpAddress();
            String userAgent = AuditContextHolder.getUserAgent();

            Map<String, Object> metadata = extractMetadata(joinPoint, result, auditable, startTime);
            AuditDetails details = AuditDetails.of(null, null, metadata);

            CreateAuditLogCommand command = new CreateAuditLogCommand(
                    auditable.entityType(),
                    entityId,
                    auditable.action(),
                    userId != null ? UserId.of(userId) : null,
                    ipAddress,
                    userAgent,
                    details
            );

            auditLogService.createAuditLog(command);

            log.debug("감사 로그 생성 완료: entityType={}, entityId={}, action={}, userId={}",
                    auditable.entityType(),
                    entityId,
                    auditable.action(),
                    userId);

        } catch (Exception e) {
            // 감사 로그 생성 실패는 비즈니스 로직에 영향을 주지 않음
            log.error("감사 로그 생성 실패 (비즈니스 로직은 정상 처리됨): method={}, action={}",
                    joinPoint.getSignature().toShortString(),
                    auditable.action(),
                    e);
        }

        return result;
    }

    /**
     * SpEL 표현식을 사용하여 엔티티 ID 추출
     */
    private String extractEntityId(
            ProceedingJoinPoint joinPoint,
            Object result,
            Auditable auditable
    ) {
        if (auditable.entityIdParam().isEmpty()) {
            return null;
        }

        EvaluationContext context = createEvaluationContext(joinPoint, result);

        try {
            Object value = parser.parseExpression(auditable.entityIdParam()).getValue(context);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.warn("엔티티 ID 추출 실패: expression={}, error={}", auditable.entityIdParam(), e.getMessage());
            return null;
        }
    }

    /**
     * SpEL 평가 컨텍스트 생성
     */
    private EvaluationContext createEvaluationContext(
            ProceedingJoinPoint joinPoint,
            Object result
    ) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 반환값 추가
        context.setVariable("result", result);

        // 메서드 파라미터 추가
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        // 파라미터 배열 추가
        context.setVariable("args", args);

        return context;
    }

    /**
     * SecurityContextHolder로부터 인증된 사용자 ID 추출
     *
     * SecurityContextHolder는 JwtAuthenticationFilter에서 설정되며,
     * 인증되지 않은 요청의 경우 null을 반환하여 감사 로그에 userId=null로 기록됨
     */
    private String extractUserId(ProceedingJoinPoint joinPoint) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.debug("인증된 사용자가 없습니다. (감사 로그에는 userId=null로 기록됨)");
                return null;
            }

            Object principal = authentication.getPrincipal();
            if (principal instanceof SecurityUser securityUser) {
                String userId = securityUser.getUserId();
                log.debug("SecurityContextHolder에서 사용자 ID 추출: userId={}", userId);
                return userId;
            }

            log.debug("예상 외의 principal 타입: type={}", principal.getClass().getSimpleName());
            return null;

        } catch (Exception e) {
            // SecurityContextHolder 접근 중 예외 발생 (매우 드문 경우)
            log.warn("SecurityContextHolder 접근 중 예외 발생 (감사 로그에는 userId=null로 기록됨)", e);
            return null;
        }
    }

    /**
     * 메타데이터 추출
     */
    private Map<String, Object> extractMetadata(
            ProceedingJoinPoint joinPoint,
            Object result,
            Auditable auditable,
            long startTime
    ) {
        Map<String, Object> metadata = new HashMap<>();

        // 기본 메타데이터
        metadata.put("method", joinPoint.getSignature().toShortString());
        metadata.put("executionTime", System.currentTimeMillis() - startTime);
        metadata.put("timestamp", System.currentTimeMillis());

        // 반환값 포함 옵션
        if (auditable.includeResult() && result != null) {
            metadata.put("result", result);
        }

        // 파라미터 정보 (민감 정보 제외)
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            Map<String, Object> safeParams = new HashMap<>();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();

            for (int i = 0; i < paramNames.length; i++) {
                String paramName = paramNames[i];
                Object paramValue = args[i];

                // 민감 정보 마스킹
                if (paramValue != null && isSensitiveParameter(paramName)) {
                    safeParams.put(paramName, "***MASKED***");
                } else {
                    safeParams.put(paramName, paramValue);
                }
            }
            metadata.put("parameters", safeParams);
        }

        return metadata;
    }

    /**
     * 민감 파라미터 여부 확인
     */
    private boolean isSensitiveParameter(String paramName) {
        if (paramName == null) {
            return false;
        }
        String lowerParam = paramName.toLowerCase();
        return lowerParam.contains("password")
                || lowerParam.contains("token")
                || lowerParam.contains("secret")
                || lowerParam.contains("signature");
    }
}