package com.signly.notification.application;

import com.signly.contract.application.ContractPdfService;
import com.signly.contract.domain.model.Contract;
import com.signly.contract.domain.model.GeneratedPdf;
import com.signly.document.application.DocumentService;
import com.signly.notification.domain.model.EmailAttachment;
import com.signly.notification.domain.model.EmailOutbox;
import com.signly.notification.domain.model.EmailTemplate;
import com.signly.notification.domain.repository.EmailOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final EmailOutboxRepository outboxRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private final String baseUrl;

    @Value("${app.name:Signly}")
    private final String companyName;

    private final ContractPdfService contractPdfService;
    private final DocumentService documentService;

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

            outboxRepository.save(outbox);
            logger.info("계약서 서명 요청 이메일을 Outbox에 저장: contractId={}, outboxId={}",
                    contract.getId().value(), outbox.getId().value());

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

            outboxRepository.save(firstPartyOutbox);
            outboxRepository.save(secondPartyOutbox);

            if (generatedPdf != null) {
                try {
                    documentService.storeContractPdf(contract, generatedPdf);
                } catch (Exception storeEx) {
                    logger.error("계약서 PDF 저장 실패: contractId={}", contract.getId().value(), storeEx);
                }
            }

            logger.info("계약서 완료 알림 이메일을 Outbox에 저장: contractId={}, PDF첨부={}",
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

            outboxRepository.save(outbox);
            logger.info("계약서 취소 알림 이메일을 Outbox에 저장: contractId={}", contract.getId().value());

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

            outboxRepository.save(firstPartyOutbox);
            outboxRepository.save(secondPartyOutbox);

            logger.info("계약서 만료 알림 이메일을 Outbox에 저장: contractId={}", contract.getId().value());

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

            outboxRepository.save(outbox);
            logger.info("계약서 만료 임박 알림 이메일을 Outbox에 저장: contractId={}, daysLeft={}", contract.getId().value(), daysLeft);

        } catch (Exception e) {
            logger.error("계약서 만료 임박 알림 이메일 Outbox 저장 실패: {}", contract.getId().value(), e);
        }
    }

}
