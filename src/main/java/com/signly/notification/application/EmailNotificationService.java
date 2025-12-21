package com.signly.notification.application;

import com.signly.contract.application.ContractPdfService;
import com.signly.contract.domain.model.Contract;
import com.signly.contract.domain.model.GeneratedPdf;
import com.signly.document.application.DocumentService;
import com.signly.notification.domain.event.EmailOutboxCreatedEvent;
import com.signly.notification.domain.model.EmailAttachment;
import com.signly.notification.domain.model.EmailOutbox;
import com.signly.notification.domain.model.EmailTemplate;
import com.signly.notification.domain.repository.EmailOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final EmailOutboxRepository outboxRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final String baseUrl;
    private final String companyName;
    private final ContractPdfService contractPdfService;
    private final DocumentService documentService;

    public EmailNotificationService(
            EmailOutboxRepository outboxRepository,
            ApplicationEventPublisher eventPublisher,
            @Value("${app.base-url:http://localhost:8080}") String baseUrl,
            @Value("${app.name:Signly}") String companyName,
            ContractPdfService contractPdfService,
            DocumentService documentService
    ) {
        this.outboxRepository = outboxRepository;
        this.eventPublisher = eventPublisher;
        this.baseUrl = baseUrl;
        this.companyName = companyName;
        this.contractPdfService = contractPdfService;
        this.documentService = documentService;
    }

    @Transactional
    public void sendContractSigningRequest(Contract contract) {
        try {
            String signingUrl = baseUrl + "/sign/" + contract.getSignToken().value();
            var variables = new HashMap<String, Object>();
            variables.put("contractTitle", contract.getTitle());
            variables.put("firstPartyName", contract.getFirstParty().name());
            variables.put("firstPartyEmail", contract.getFirstParty().email());
            variables.put("secondPartyName", contract.getSecondParty().name());
            variables.put("signerName", contract.getSecondParty().name());
            variables.put("contractUrl", signingUrl);
            variables.put("expiresAt", contract.getExpiresAt());
            variables.put("companyName", companyName);

            var outbox = EmailOutbox.create(
                    EmailTemplate.CONTRACT_SIGNING_REQUEST,
                    contract.getSecondParty().email(),
                    contract.getSecondParty().name(),
                    variables
            );

            EmailOutbox saved = outboxRepository.save(outbox);

            // 이벤트 발행 - 트랜잭션 커밋 후 처리됨
            eventPublisher.publishEvent(new EmailOutboxCreatedEvent(saved.getId()));

            logger.info("계약서 서명 요청 이메일을 Outbox에 저장 및 이벤트 발행: contractId={}, outboxId={}", contract.getId().value(), saved.getId().value());

        } catch (Exception e) {
            logger.error("계약서 서명 요청 이메일 Outbox 저장 실패: {}", contract.getId().value(), e);
        }
    }

    @Transactional
    public void sendContractCompleted(Contract contract) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("contractTitle", contract.getTitle());
            variables.put("firstPartyName", contract.getFirstParty().name());
            variables.put("secondPartyName", contract.getSecondParty().name());
            variables.put("completedAt", contract.getUpdatedAt());
            variables.put("companyName", companyName);

            // 서명 이미지는 PDF에 포함되므로 이메일 템플릿 변수에는 포함하지 않음
            // (template_variables 컬럼 크기 제한 고려)

            // PDF 생성 및 첨부파일 준비
            var attachments = new ArrayList<EmailAttachment>();
            GeneratedPdf generatedPdf = null;
            try {
                generatedPdf = contractPdfService.generateContractPdf(contract.getId().value());
                var pdfAttachment = EmailAttachment.of(
                        generatedPdf.fileName(),
                        generatedPdf.content(),
                        generatedPdf.getContentType()
                );
                attachments.add(pdfAttachment);
                logger.info("계약서 PDF 생성 및 첨부 준비 완료: contractId={}, fileName={}",
                        contract.getId().value(), generatedPdf.fileName());
            } catch (Exception ex) {
                logger.error("계약서 PDF 생성 실패, PDF 없이 이메일 발송: contractId={}",
                        contract.getId().value(), ex);
            }

            // 양 당사자에게 Outbox 저장 (PDF 첨부)
            var firstPartyOutbox = EmailOutbox.create(
                    EmailTemplate.CONTRACT_COMPLETED,
                    contract.getFirstParty().email(),
                    contract.getFirstParty().name(),
                    variables,
                    attachments
            );

            var secondPartyOutbox = EmailOutbox.create(
                    EmailTemplate.CONTRACT_COMPLETED,
                    contract.getSecondParty().email(),
                    contract.getSecondParty().name(),
                    variables,
                    attachments
            );

            EmailOutbox savedFirstParty = outboxRepository.save(firstPartyOutbox);
            EmailOutbox savedSecondParty = outboxRepository.save(secondPartyOutbox);

            // 이벤트 발행 - 트랜잭션 커밋 후 처리됨
            eventPublisher.publishEvent(new EmailOutboxCreatedEvent(savedFirstParty.getId()));
            eventPublisher.publishEvent(new EmailOutboxCreatedEvent(savedSecondParty.getId()));

            if (generatedPdf != null) {
                try {
                    documentService.storeContractPdf(contract, generatedPdf);
                } catch (Exception storeEx) {
                    logger.error("계약서 PDF 저장 실패: contractId={}", contract.getId().value(), storeEx);
                }
            }

            logger.info("계약서 완료 알림 이메일을 Outbox에 저장 및 이벤트 발행: contractId={}, PDF첨부={}",
                    contract.getId().value(), !attachments.isEmpty());

        } catch (Exception e) {
            logger.error("계약서 완료 알림 이메일 Outbox 저장 실패: {}", contract.getId().value(), e);
        }
    }

    @Transactional
    public void sendContractCancelled(Contract contract) {
        try {
            var variables = new HashMap<String, Object>();
            variables.put("contractTitle", contract.getTitle());
            variables.put("firstPartyName", contract.getFirstParty().name());
            variables.put("secondPartyName", contract.getSecondParty().name());
            variables.put("cancelledAt", contract.getUpdatedAt());
            variables.put("companyName", companyName);

            var outbox = EmailOutbox.create(
                    EmailTemplate.CONTRACT_CANCELLED,
                    contract.getSecondParty().email(),
                    contract.getSecondParty().name(),
                    variables
            );

            EmailOutbox saved = outboxRepository.save(outbox);

            // 이벤트 발행 - 트랜잭션 커밋 후 처리됨
            eventPublisher.publishEvent(new EmailOutboxCreatedEvent(saved.getId()));

            logger.info("계약서 취소 알림 이메일을 Outbox에 저장 및 이벤트 발행: contractId={}", contract.getId().value());

        } catch (Exception e) {
            logger.error("계약서 취소 알림 이메일 Outbox 저장 실패: {}", contract.getId().value(), e);
        }
    }

    @Transactional
    public void sendContractExpired(Contract contract) {
        try {
            var variables = new HashMap<String, Object>();
            variables.put("contractTitle", contract.getTitle());
            variables.put("firstPartyName", contract.getFirstParty().name());
            variables.put("secondPartyName", contract.getSecondParty().name());
            variables.put("expiredAt", contract.getExpiresAt());
            variables.put("companyName", companyName);

            // 양 당사자에게 Outbox 저장
            var firstPartyOutbox = EmailOutbox.create(
                    EmailTemplate.CONTRACT_EXPIRED,
                    contract.getFirstParty().email(),
                    contract.getFirstParty().name(),
                    variables
            );

            var secondPartyOutbox = EmailOutbox.create(
                    EmailTemplate.CONTRACT_EXPIRED,
                    contract.getSecondParty().email(),
                    contract.getSecondParty().name(),
                    variables
            );

            EmailOutbox savedFirstParty = outboxRepository.save(firstPartyOutbox);
            EmailOutbox savedSecondParty = outboxRepository.save(secondPartyOutbox);

            // 이벤트 발행 - 트랜잭션 커밋 후 처리됨
            eventPublisher.publishEvent(new EmailOutboxCreatedEvent(savedFirstParty.getId()));
            eventPublisher.publishEvent(new EmailOutboxCreatedEvent(savedSecondParty.getId()));

            logger.info("계약서 만료 알림 이메일을 Outbox에 저장 및 이벤트 발행: contractId={}", contract.getId().value());

        } catch (Exception e) {
            logger.error("계약서 만료 알림 이메일 Outbox 저장 실패: {}", contract.getId().value(), e);
        }
    }

    @Transactional
    public void sendExpirationWarning(
            Contract contract,
            int daysLeft
    ) {
        try {
            String signingUrl = baseUrl + "/sign/" + contract.getSignToken().value();

            var variables = new HashMap<String, Object>();
            variables.put("contractTitle", contract.getTitle());
            variables.put("signerName", contract.getSecondParty().name());
            variables.put("daysLeft", daysLeft);
            variables.put("expiresAt", contract.getExpiresAt());
            variables.put("contractUrl", signingUrl);
            variables.put("companyName", companyName);

            var outbox = EmailOutbox.create(
                    EmailTemplate.EXPIRATION_WARNING,
                    contract.getSecondParty().email(),
                    contract.getSecondParty().name(),
                    variables
            );

            EmailOutbox saved = outboxRepository.save(outbox);

            // 이벤트 발행 - 트랜잭션 커밋 후 처리됨
            eventPublisher.publishEvent(new EmailOutboxCreatedEvent(saved.getId()));

            logger.info("계약서 만료 임박 알림 이메일을 Outbox에 저장 및 이벤트 발행: contractId={}, daysLeft={}", contract.getId().value(), daysLeft);

        } catch (Exception e) {
            logger.error("계약서 만료 임박 알림 이메일 Outbox 저장 실패: {}", contract.getId().value(), e);
        }
    }

    /**
     * 이메일 인증 메일 발송
     *
     * @param email             수신자 이메일
     * @param userName          사용자 이름
     * @param verificationToken 인증 토큰
     */
    @Transactional
    public void sendEmailVerification(
            String email,
            String userName,
            String verificationToken
    ) {
        try {
            String verificationUrl = baseUrl + "/verify-email?token=" + verificationToken;

            var variables = new HashMap<String, Object>();
            variables.put("userName", userName);
            variables.put("verificationUrl", verificationUrl);
            variables.put("expiryHours", "24");
            variables.put("companyName", companyName);

            var outbox = EmailOutbox.create(
                    EmailTemplate.EMAIL_VERIFICATION,
                    email,
                    userName,
                    variables
            );

            EmailOutbox saved = outboxRepository.save(outbox);

            // 이벤트 발행 - 트랜잭션 커밋 후 처리됨
            eventPublisher.publishEvent(new EmailOutboxCreatedEvent(saved.getId()));

            logger.info("이메일 인증 메일을 Outbox에 저장 및 이벤트 발행: email={}, outboxId={}", email, saved.getId().value());

        } catch (Exception e) {
            logger.error("이메일 인증 메일 Outbox 저장 실패: email={}", email, e);
        }
    }

}
