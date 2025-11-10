package com.signly.contract.application;

import com.signly.common.email.EmailService;
import com.signly.contract.domain.model.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ContractEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(ContractEventHandler.class);

    private final EmailService emailService;

    public ContractEventHandler(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @EventListener
    public void handleContractSentForSigning(ContractSentForSigningEvent event) {
        logger.info("계약서 서명 요청 이벤트 처리: {}", event.getContract().getTitle());

        try {
            Contract contract = event.getContract();
            String contractUrl = generateContractUrl(contract);

            // 모든 서명 대상자에게 이메일 발송
            contract.getPendingSigners().forEach(signerEmail -> {
                try {
                    emailService.sendContractSigningRequest(
                        signerEmail,
                        contract.getTitle(),
                        getSignerName(signerEmail, contract),
                        contractUrl
                    );
                    logger.info("서명 요청 이메일 발송 완료: {}", signerEmail);
                } catch (Exception e) {
                    logger.error("서명 요청 이메일 발송 실패: {}", signerEmail, e);
                }
            });

        } catch (Exception e) {
            logger.error("계약서 서명 요청 이벤트 처리 중 오류 발생", e);
        }
    }

    @Async
    @EventListener
    public void handleContractSigned(ContractSignedEvent event) {
        logger.info("계약서 서명 완료 이벤트 처리: {}", event.getContract().getTitle());

        try {
            Contract contract = event.getContract();
            String signerName = event.getSignerName();

            // 계약서 작성자에게 서명 완료 알림
            // UserRepository를 통해 작성자 정보를 가져와야 하지만
            // 여기서는 간단히 로그만 남김
            logger.info("계약서 '{}' 서명 완료: {}", contract.getTitle(), signerName);

            // 필요시 다른 당사자들에게도 알림 발송
            emailService.sendContractSigned(
                "creator@example.com", // 실제로는 작성자 이메일을 조회해야 함
                contract.getTitle(),
                signerName
            );

        } catch (Exception e) {
            logger.error("계약서 서명 완료 이벤트 처리 중 오류 발생", e);
        }
    }

    @Async
    @EventListener
    public void handleContractCompleted(ContractCompletedEvent event) {
        logger.info("계약서 완료 이벤트 처리: {}", event.getContract().getTitle());

        try {
            Contract contract = event.getContract();

            // 모든 당사자에게 완료 알림
            emailService.sendContractCompleted(
                contract.getFirstParty().email(),
                contract.getTitle()
            );

            emailService.sendContractCompleted(
                contract.getSecondParty().email(),
                contract.getTitle()
            );

            logger.info("계약서 완료 알림 발송 완료: {}", contract.getTitle());

        } catch (Exception e) {
            logger.error("계약서 완료 이벤트 처리 중 오류 발생", e);
        }
    }

    @Async
    @EventListener
    public void handleContractCancelled(ContractCancelledEvent event) {
        logger.info("계약서 취소 이벤트 처리: {}", event.getContract().getTitle());

        try {
            Contract contract = event.getContract();
            String reason = event.getReason();

            // 모든 당사자에게 취소 알림
            emailService.sendContractCancelled(
                contract.getFirstParty().email(),
                contract.getTitle(),
                reason
            );

            emailService.sendContractCancelled(
                contract.getSecondParty().email(),
                contract.getTitle(),
                reason
            );

            logger.info("계약서 취소 알림 발송 완료: {}", contract.getTitle());

        } catch (Exception e) {
            logger.error("계약서 취소 이벤트 처리 중 오류 발생", e);
        }
    }

    private String generateContractUrl(Contract contract) {
        // 실제로는 application.properties에서 base URL을 가져와야 함
        return "https://signly.com/contracts/" + contract.getId().getValue() + "/sign";
    }

    private String getSignerName(String signerEmail, Contract contract) {
        if (contract.getFirstParty().email().equals(signerEmail)) {
            return contract.getFirstParty().name();
        } else if (contract.getSecondParty().email().equals(signerEmail)) {
            return contract.getSecondParty().name();
        }
        return "서명자";
    }
}