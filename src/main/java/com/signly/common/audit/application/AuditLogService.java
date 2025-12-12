package com.signly.common.audit.application;

import com.signly.common.audit.application.dto.CreateAuditLogCommand;
import com.signly.common.audit.domain.model.AuditLog;
import com.signly.common.audit.domain.repository.AuditLogRepository;
import com.signly.user.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 감사 로그 Application Service
 * <p>
 * REQUIRES_NEW 트랜잭션을 사용하여 메인 트랜잭션과 독립적으로 커밋
 * 감사 로그 실패가 비즈니스 로직에 영향을 주지 않도록 함
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * 감사 로그 생성
     *
     * @param command 감사 로그 생성 커맨드
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createAuditLog(CreateAuditLogCommand command) {
        try {
            AuditLog auditLog = AuditLog.create(
                    command.entityType(),
                    command.entityId(),
                    command.action(),
                    command.userId(),
                    command.ipAddress(),
                    command.userAgent(),
                    command.details()
            );

            auditLogRepository.save(auditLog);

            log.debug("감사 로그 생성 완료: entityType={}, entityId={}, action={}, userId={}",
                    command.entityType(),
                    command.entityId(),
                    command.action(),
                    command.userId() != null ? command.userId().value() : "SYSTEM");

        } catch (Exception e) {
            // 감사 로그 실패는 비즈니스 로직에 영향을 주지 않음
            log.error("감사 로그 생성 실패: entityType={}, entityId={}, action={}",
                    command.entityType(),
                    command.entityId(),
                    command.action(),
                    e);
        }
    }

    /**
     * 감사 로그 생성 (간편 메서드)
     *
     * @param entityType 엔티티 타입
     * @param entityId   엔티티 ID
     * @param action     작업
     * @param userId     사용자 ID (nullable)
     * @param ipAddress  IP 주소 (nullable)
     * @param userAgent  User-Agent (nullable)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createAuditLog(
            com.signly.common.audit.domain.model.EntityType entityType,
            String entityId,
            com.signly.common.audit.domain.model.AuditAction action,
            UserId userId,
            String ipAddress,
            String userAgent
    ) {
        CreateAuditLogCommand command = new CreateAuditLogCommand(
                entityType,
                entityId,
                action,
                userId,
                ipAddress,
                userAgent,
                com.signly.common.audit.domain.model.AuditDetails.empty()
        );

        createAuditLog(command);
    }
}