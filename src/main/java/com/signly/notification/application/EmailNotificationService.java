package com.signly.notification.application;

import com.signly.contract.domain.model.Contract;
import com.signly.notification.domain.model.EmailOutbox;
import com.signly.notification.domain.model.EmailTemplate;
import com.signly.notification.domain.repository.EmailOutboxRepository;
import com.signly.signature.application.FirstPartySignatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final EmailOutboxRepository outboxRepository;
    private final String baseUrl;
    private final String companyName;
    private final FirstPartySignatureService firstPartySignatureService;

    public EmailNotificationService(
            EmailOutboxRepository outboxRepository,
            @Value("${app.base-url:http://localhost:8080}") String baseUrl,
            @Value("${app.name:Signly}") String companyName,
            FirstPartySignatureService firstPartySignatureService) {
        this.outboxRepository = outboxRepository;
        this.baseUrl = baseUrl;
        this.companyName = companyName;
        this.firstPartySignatureService = firstPartySignatureService;
    }

    @Transactional
    public void sendContractSigningRequest(Contract contract) {
        try {
            String signingUrl = baseUrl + "/sign/" + contract.getSignToken().value();

            Map<String, Object> variables = new HashMap<>();
            variables.put("contractTitle", contract.getTitle());
            variables.put("firstPartyName", contract.getFirstParty().getName());
            variables.put("firstPartyEmail", contract.getFirstParty().getEmail());
            variables.put("secondPartyName", contract.getSecondParty().getName());
            variables.put("signerName", contract.getSecondParty().getName());
            variables.put("contractUrl", signingUrl);
            variables.put("expiresAt", contract.getExpiresAt());
            variables.put("companyName", companyName);

            EmailOutbox outbox = EmailOutbox.create(
                    EmailTemplate.CONTRACT_SIGNING_REQUEST,
                    contract.getSecondParty().getEmail(),
                    contract.getSecondParty().getName(),
                    variables
            );

            outboxRepository.save(outbox);
            logger.info("계약서 서명 요청 이메일을 Outbox에 저장: contractId={}, outboxId={}",
                    contract.getId().getValue(), outbox.getId().getValue());

        } catch (Exception e) {
            logger.error("계약서 서명 요청 이메일 Outbox 저장 실패: {}", contract.getId().getValue(), e);
        }
    }

    @Transactional
    public void sendContractCompleted(Contract contract) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("contractTitle", contract.getTitle());
            variables.put("firstPartyName", contract.getFirstParty().getName());
            variables.put("secondPartyName", contract.getSecondParty().getName());
            variables.put("completedAt", contract.getUpdatedAt());
            variables.put("companyName", companyName);

            try {
                String dataUrl = firstPartySignatureService.getSignatureDataUrl(contract.getCreatorId().getValue());
                variables.put("firstPartySignatureImage", dataUrl);
            } catch (Exception ex) {
                logger.warn("갑 서명을 이메일에 첨부하지 못했습니다: contractId={}", contract.getId().getValue(), ex);
            }

            // 양 당사자에게 Outbox 저장
            EmailOutbox firstPartyOutbox = EmailOutbox.create(
                    EmailTemplate.CONTRACT_COMPLETED,
                    contract.getFirstParty().getEmail(),
                    contract.getFirstParty().getName(),
                    variables
            );

            EmailOutbox secondPartyOutbox = EmailOutbox.create(
                    EmailTemplate.CONTRACT_COMPLETED,
                    contract.getSecondParty().getEmail(),
                    contract.getSecondParty().getName(),
                    variables
            );

            outboxRepository.save(firstPartyOutbox);
            outboxRepository.save(secondPartyOutbox);

            logger.info("계약서 완료 알림 이메일을 Outbox에 저장: contractId={}", contract.getId().getValue());

        } catch (Exception e) {
            logger.error("계약서 완료 알림 이메일 Outbox 저장 실패: {}", contract.getId().getValue(), e);
        }
    }

    @Transactional
    public void sendContractCancelled(Contract contract) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("contractTitle", contract.getTitle());
            variables.put("firstPartyName", contract.getFirstParty().getName());
            variables.put("secondPartyName", contract.getSecondParty().getName());
            variables.put("cancelledAt", contract.getUpdatedAt());
            variables.put("companyName", companyName);

            EmailOutbox outbox = EmailOutbox.create(
                    EmailTemplate.CONTRACT_CANCELLED,
                    contract.getSecondParty().getEmail(),
                    contract.getSecondParty().getName(),
                    variables
            );

            outboxRepository.save(outbox);
            logger.info("계약서 취소 알림 이메일을 Outbox에 저장: contractId={}", contract.getId().getValue());

        } catch (Exception e) {
            logger.error("계약서 취소 알림 이메일 Outbox 저장 실패: {}", contract.getId().getValue(), e);
        }
    }

    @Transactional
    public void sendContractExpired(Contract contract) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("contractTitle", contract.getTitle());
            variables.put("firstPartyName", contract.getFirstParty().getName());
            variables.put("secondPartyName", contract.getSecondParty().getName());
            variables.put("expiredAt", contract.getExpiresAt());
            variables.put("companyName", companyName);

            // 양 당사자에게 Outbox 저장
            EmailOutbox firstPartyOutbox = EmailOutbox.create(
                    EmailTemplate.CONTRACT_EXPIRED,
                    contract.getFirstParty().getEmail(),
                    contract.getFirstParty().getName(),
                    variables
            );

            EmailOutbox secondPartyOutbox = EmailOutbox.create(
                    EmailTemplate.CONTRACT_EXPIRED,
                    contract.getSecondParty().getEmail(),
                    contract.getSecondParty().getName(),
                    variables
            );

            outboxRepository.save(firstPartyOutbox);
            outboxRepository.save(secondPartyOutbox);

            logger.info("계약서 만료 알림 이메일을 Outbox에 저장: contractId={}", contract.getId().getValue());

        } catch (Exception e) {
            logger.error("계약서 만료 알림 이메일 Outbox 저장 실패: {}", contract.getId().getValue(), e);
        }
    }
}
