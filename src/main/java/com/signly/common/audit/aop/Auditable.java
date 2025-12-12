package com.signly.common.audit.aop;

import com.signly.common.audit.domain.model.AuditAction;
import com.signly.common.audit.domain.model.EntityType;

import java.lang.annotation.*;

/**
 * 감사 로그 자동 생성 애노테이션
 * <p>
 * 사용 예시:
 * <pre>
 * {@code
 * @Auditable(
 *     entityType = EntityType.CONTRACT,
 *     action = AuditAction.CONTRACT_CREATED,
 *     entityIdParam = "#result.id.value"
 * )
 * public ContractResponse createContract(String userId, CreateContractCommand command) {
 *     // ...
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /**
     * 대상 엔티티 타입
     */
    EntityType entityType();

    /**
     * 수행된 작업
     */
    AuditAction action();

    /**
     * 엔티티 ID 추출 SpEL 표현식
     * <p>
     * 사용 가능한 변수:
     * - #args: 메서드 파라미터 배열
     * - #paramName: 파라미터 이름
     * - #result: 메서드 반환값
     * <p>
     * 예시:
     * - "#contractId" (파라미터)
     * - "#result.id.value" (반환값의 필드)
     * - "#args[0]" (첫 번째 파라미터)
     */
    String entityIdParam() default "";

    /**
     * 반환값을 메타데이터에 포함할지 여부
     */
    boolean includeResult() default false;
}