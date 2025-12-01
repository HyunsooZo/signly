package com.deally.contract.application;

import com.deally.common.exception.NotFoundException;
import com.deally.common.exception.ValidationException;
import com.deally.common.storage.FileStorageService;
import com.deally.contract.application.dto.CreateSignatureCommand;
import com.deally.contract.application.dto.SignContractCommand;
import com.deally.contract.domain.model.Contract;
import com.deally.contract.domain.model.ContractId;
import com.deally.contract.domain.model.ContractStatus;
import com.deally.contract.domain.repository.ContractRepository;
import com.deally.contract.domain.repository.SignatureRepository;
import com.deally.contract.domain.service.ContractSigningService;
import com.deally.notification.application.EmailNotificationService;
import com.deally.signature.application.FirstPartySignatureService;
import com.deally.signature.application.SignatureService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 계약서 서명 프로세스 조율 서비스
 * SRP: 서명 관련 프로세스 조율 책임만 담당
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ContractSigningCoordinator {

    private static final Logger logger = LoggerFactory.getLogger(ContractSigningCoordinator.class);

    private final ContractRepository contractRepository;
    private final ContractSigningService contractSigningService;
    private final FirstPartySignatureService firstPartySignatureService;
    private final SignatureService signatureService;
    private final SignatureRepository signatureRepository;
    private final EmailNotificationService emailNotificationService;
    private final ContractAuthorizationService authorizationService;
    private final ContractPdfService contractPdfService;
    private final FileStorageService fileStorageService;

    public void sendForSigning(
            String userId,
            String contractId
    ) {
        Contract contract = findContract(contractId);
        authorizationService.validateOwnership(userId, contract);

        // 1. 제1 당사자 서명 확인
        firstPartySignatureService.ensureSignatureExists(contract.getCreatorId().value());

        // 2. 제1 당사자 서명 저장
        saveFirstPartySignature(contract);

        // 3. 계약서 상태 변경
        contract.sendForSigning();
        Contract savedContract = contractRepository.save(contract);

        // 4. 제2 당사자에게 이메일 발송
        emailNotificationService.sendContractSigningRequest(savedContract);

    }

    public void resendSigningEmail(
            String userId,
            String contractId
    ) {
        Contract contract = findContract(contractId);
        authorizationService.validateOwnership(userId, contract);

        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new ValidationException("서명 대기 상태에서만 서명 요청을 재전송할 수 있습니다");
        }

        // 제1 당사자 서명 확인 및 저장 (기존 데이터 보정)
        ensureFirstPartySignatureExists(contract);

        emailNotificationService.sendContractSigningRequest(contract);

    }

    public Contract signContract(
            String signerEmail,
            String contractId,
            SignContractCommand command
    ) {
        var contract = findContract(contractId);

        var request = new ContractSigningService.SigningRequest(signerEmail, command.signerName(), command.signatureData(), command.ipAddress());

        var result = contractSigningService.processSigning(contract, request);
        var savedContract = contractRepository.save(contract);

        if (result.isFullySigned()) {
            // PDF 생성 및 저장
            try {
                savePdfForCompletedContract(savedContract);
            } catch (Exception pdfEx) {
                logger.error("PDF 저장 중 오류 발생 (서명 처리는 완료됨): contractId={}",
                        contract.getId().value(), pdfEx);
            }

            // 완료 알림 이메일 발송
            emailNotificationService.sendContractCompleted(savedContract);
        }

        return savedContract;
    }

    public Contract processSignature(
            String token,
            String signerEmail,
            String signerName,
            String signatureData,
            String ipAddress
    ) {
        var contract = contractRepository.findBySignToken(com.deally.contract.domain.model.SignToken.of(token))
                .orElseThrow(() -> new NotFoundException("유효하지 않은 서명 링크입니다"));

        var contractId = contract.getId();

        // 서명 데이터 저장
        var command = new CreateSignatureCommand(
                contractId.value(),
                signatureData,
                signerEmail,
                signerName,
                ipAddress,
                null
        );
        signatureService.createSignature(command);

        // 서명 처리 및 상태 업데이트
        var request = new ContractSigningService.SigningRequest(signerEmail, signerName, signatureData, ipAddress);

        var result = contractSigningService.processSigning(contract, request);
        var savedContract = contractRepository.save(contract);

        if (result.isFullySigned()) {
            logger.info("모든 서명 완료, PDF 저장 및 완료 알림 이메일 발송: contractId={}", contract.getId().value());

            // PDF 생성 및 저장
            try {
                savePdfForCompletedContract(savedContract);
            } catch (Exception pdfEx) {
                logger.error("PDF 저장 중 오류 발생 (서명 처리는 완료됨): contractId={}",
                        contract.getId().value(), pdfEx);
            }

            // 완료 알림 이메일 발송
            try {
                emailNotificationService.sendContractCompleted(savedContract);
            } catch (Exception emailEx) {
                logger.error("완료 알림 이메일 발송 중 오류 발생 (서명 처리는 완료됨): contractId={}",
                        contract.getId().value(), emailEx);
            }
        }

        return savedContract;
    }

    /**
     * 완료된 계약서의 PDF를 생성하고 저장
     */
    private void savePdfForCompletedContract(Contract contract) {
        try {
            String contractId = contract.getId().value();
            logger.info("완료된 계약서 PDF 생성 시작: contractId={}", contractId);

            // PDF 생성
            var pdf = contractPdfService.generateContractPdf(contractId);

            // 파일 저장
            var storedFile = fileStorageService.storeFile(
                    pdf.content(),
                    pdf.fileName(),
                    "application/pdf",
                    "contracts/completed"
            );

            // 계약서에 PDF 경로 저장
            contract.setPdfPath(storedFile.filePath());
            contractRepository.save(contract);

            logger.info("완료된 계약서 PDF 저장 완료: contractId={}, path={}", contractId, storedFile.filePath());
        } catch (Exception e) {
            logger.error("PDF 저장 실패: contractId={}", contract.getId().value(), e);
            throw new RuntimeException("PDF 저장 중 오류가 발생했습니다", e);
        }
    }

    public void cancelContract(
            String userId,
            String contractId
    ) {
        var contract = findContract(contractId);
        authorizationService.validateOwnership(userId, contract);

        contract.cancel();
        var savedContract = contractRepository.save(contract);

        emailNotificationService.sendContractCancelled(savedContract);

    }

    public void expireContracts() {
        var now = LocalDateTime.now();
        var expiredContracts = contractRepository.findExpiredContracts(now);

        expiredContracts.forEach(contract -> {
            contract.expire();
            Contract savedContract = contractRepository.save(contract);
            emailNotificationService.sendContractExpired(savedContract);
        });
    }

    private Contract findContract(String contractId) {
        return contractRepository.findById(ContractId.of(contractId))
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));
    }

    private void saveFirstPartySignature(Contract contract) {
        if (signatureRepository.existsByContractIdAndSignerEmail(
                contract.getId(),
                contract.getFirstParty().email())) {
            logger.info("제1 당사자 서명이 이미 존재합니다. 저장 생략: contractId={}, email={}",
                    contract.getId().value(), contract.getFirstParty().email());
            return;
        }

        String firstPartySignatureData = firstPartySignatureService.getSignatureDataUrl(
                contract.getCreatorId().value());

        var firstPartyCommand = new CreateSignatureCommand(
                contract.getId().value(),
                firstPartySignatureData,
                contract.getFirstParty().email(),
                contract.getFirstParty().name(),
                "SERVER",
                "Server-initiated signature"
        );
        signatureService.createSignature(firstPartyCommand);

        logger.info("제1 당사자 서명 저장 완료: contractId={}, email={}",
                contract.getId().value(), contract.getFirstParty().email());
    }

    private void ensureFirstPartySignatureExists(Contract contract) {
        firstPartySignatureService.ensureSignatureExists(contract.getCreatorId().value());

        ContractId cId = contract.getId();
        if (!signatureRepository.existsByContractIdAndSignerEmail(cId, contract.getFirstParty().email())) {
            saveFirstPartySignature(contract);
            logger.info("재전송 시 제1 당사자 서명 저장: contractId={}, email={}",
                    contract.getId().value(), contract.getFirstParty().email());
        }
    }
}
