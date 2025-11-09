package com.signly.contract.application;

import com.signly.common.exception.NotFoundException;
import com.signly.common.exception.ValidationException;
import com.signly.contract.application.dto.SignContractCommand;
import com.signly.contract.domain.model.Contract;
import com.signly.contract.domain.model.ContractId;
import com.signly.contract.domain.model.ContractStatus;
import com.signly.contract.domain.repository.ContractRepository;
import com.signly.contract.domain.service.ContractSigningService;
import com.signly.notification.application.EmailNotificationService;
import com.signly.signature.application.FirstPartySignatureService;
import com.signly.signature.application.SignatureService;
import com.signly.signature.application.dto.CreateSignatureCommand;
import com.signly.signature.domain.repository.SignatureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 계약서 서명 프로세스 조율 서비스
 * SRP: 서명 관련 프로세스 조율 책임만 담당
 */
@Service
@Transactional
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
    private final com.signly.common.storage.FileStorageService fileStorageService;

    public ContractSigningCoordinator(
            ContractRepository contractRepository,
            ContractSigningService contractSigningService,
            FirstPartySignatureService firstPartySignatureService,
            SignatureService signatureService,
            SignatureRepository signatureRepository,
            EmailNotificationService emailNotificationService,
            ContractAuthorizationService authorizationService,
            ContractPdfService contractPdfService,
            com.signly.common.storage.FileStorageService fileStorageService
    ) {
        this.contractRepository = contractRepository;
        this.contractSigningService = contractSigningService;
        this.firstPartySignatureService = firstPartySignatureService;
        this.signatureService = signatureService;
        this.signatureRepository = signatureRepository;
        this.emailNotificationService = emailNotificationService;
        this.authorizationService = authorizationService;
        this.contractPdfService = contractPdfService;
        this.fileStorageService = fileStorageService;
    }

    public Contract sendForSigning(String userId, String contractId) {
        Contract contract = findContract(contractId);
        authorizationService.validateOwnership(userId, contract);

        // 1. 제1 당사자 서명 확인
        firstPartySignatureService.ensureSignatureExists(contract.getCreatorId().getValue());

        // 2. 제1 당사자 서명 저장
        saveFirstPartySignature(contract);

        // 3. 계약서 상태 변경
        contract.sendForSigning();
        Contract savedContract = contractRepository.save(contract);

        // 4. 제2 당사자에게 이메일 발송
        emailNotificationService.sendContractSigningRequest(savedContract);

        return savedContract;
    }

    public Contract resendSigningEmail(String userId, String contractId) {
        Contract contract = findContract(contractId);
        authorizationService.validateOwnership(userId, contract);

        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new ValidationException("서명 대기 상태에서만 서명 요청을 재전송할 수 있습니다");
        }

        // 제1 당사자 서명 확인 및 저장 (기존 데이터 보정)
        ensureFirstPartySignatureExists(contract);

        emailNotificationService.sendContractSigningRequest(contract);
        
        return contract;
    }

    public Contract signContract(String signerEmail, String contractId, SignContractCommand command) {
        Contract contract = findContract(contractId);

        ContractSigningService.SigningRequest request = new ContractSigningService.SigningRequest(
                signerEmail, command.signerName(), command.signatureData(), command.ipAddress());

        ContractSigningService.SigningResult result = contractSigningService.processSigning(contract, request);
        Contract savedContract = contractRepository.save(contract);

        if (result.isFullySigned()) {
            // PDF 생성 및 저장
            try {
                savePdfForCompletedContract(savedContract);
            } catch (Exception pdfEx) {
                logger.error("PDF 저장 중 오류 발생 (서명 처리는 완료됨): contractId={}",
                        contract.getId().getValue(), pdfEx);
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
        Contract contract = contractRepository.findBySignToken(com.signly.contract.domain.model.SignToken.of(token))
                .orElseThrow(() -> new NotFoundException("유효하지 않은 서명 링크입니다"));

        ContractId contractId = contract.getId();

        // 서명 데이터 저장
        CreateSignatureCommand command = new CreateSignatureCommand(
                contractId.getValue(),
                signatureData,
                signerEmail,
                signerName,
                ipAddress,
                null
        );
        signatureService.createSignature(command);

        // 서명 처리 및 상태 업데이트
        ContractSigningService.SigningRequest request = new ContractSigningService.SigningRequest(
                signerEmail, signerName, signatureData, ipAddress);

        ContractSigningService.SigningResult result = contractSigningService.processSigning(contract, request);
        Contract savedContract = contractRepository.save(contract);

        if (result.isFullySigned()) {
            logger.info("모든 서명 완료, PDF 저장 및 완료 알림 이메일 발송: contractId={}", contract.getId().getValue());
            
            // PDF 생성 및 저장
            try {
                savePdfForCompletedContract(savedContract);
            } catch (Exception pdfEx) {
                logger.error("PDF 저장 중 오류 발생 (서명 처리는 완료됨): contractId={}",
                        contract.getId().getValue(), pdfEx);
            }
            
            // 완료 알림 이메일 발송
            try {
                emailNotificationService.sendContractCompleted(savedContract);
            } catch (Exception emailEx) {
                logger.error("완료 알림 이메일 발송 중 오류 발생 (서명 처리는 완료됨): contractId={}",
                        contract.getId().getValue(), emailEx);
            }
        }

        return savedContract;
    }

    /**
     * 완료된 계약서의 PDF를 생성하고 저장
     */
    private void savePdfForCompletedContract(Contract contract) {
        try {
            String contractId = contract.getId().getValue();
            logger.info("완료된 계약서 PDF 생성 시작: contractId={}", contractId);
            
            // PDF 생성
            com.signly.contract.domain.model.GeneratedPdf pdf = contractPdfService.generateContractPdf(contractId);
            
            // 파일 저장
            com.signly.common.storage.StoredFile storedFile = fileStorageService.storeFile(
                    pdf.getContent(),
                    pdf.getFileName(),
                    "application/pdf",
                    "contracts/completed"
            );
            
            // 계약서에 PDF 경로 저장
            contract.setPdfPath(storedFile.filePath());
            contractRepository.save(contract);
            
            logger.info("완료된 계약서 PDF 저장 완료: contractId={}, path={}", contractId, storedFile.filePath());
        } catch (Exception e) {
            logger.error("PDF 저장 실패: contractId={}", contract.getId().getValue(), e);
            throw new RuntimeException("PDF 저장 중 오류가 발생했습니다", e);
        }
    }

    public Contract completeContract(String userId, String contractId) {
        Contract contract = findContract(contractId);
        authorizationService.validateOwnership(userId, contract);
        
        firstPartySignatureService.ensureSignatureExists(contract.getCreatorId().getValue());
        contract.complete();
        Contract savedContract = contractRepository.save(contract);
        
        emailNotificationService.sendContractCompleted(savedContract);
        
        return savedContract;
    }

    public Contract cancelContract(String userId, String contractId) {
        Contract contract = findContract(contractId);
        authorizationService.validateOwnership(userId, contract);
        
        contract.cancel();
        Contract savedContract = contractRepository.save(contract);
        
        emailNotificationService.sendContractCancelled(savedContract);
        
        return savedContract;
    }

    public void expireContracts() {
        LocalDateTime now = LocalDateTime.now();
        List<Contract> expiredContracts = contractRepository.findExpiredContracts(now);

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
        String firstPartySignatureData = firstPartySignatureService.getSignatureDataUrl(
                contract.getCreatorId().getValue());
        
        CreateSignatureCommand firstPartyCommand = new CreateSignatureCommand(
                contract.getId().getValue(),
                firstPartySignatureData,
                contract.getFirstParty().getEmail(),
                contract.getFirstParty().getName(),
                "SERVER",
                "Server-initiated signature"
        );
        signatureService.createSignature(firstPartyCommand);
        
        logger.info("제1 당사자 서명 저장 완료: contractId={}, email={}",
                contract.getId().getValue(), contract.getFirstParty().getEmail());
    }

    private void ensureFirstPartySignatureExists(Contract contract) {
        firstPartySignatureService.ensureSignatureExists(contract.getCreatorId().getValue());
        
        ContractId cId = contract.getId();
        if (!signatureRepository.existsByContractIdAndSignerEmail(cId, contract.getFirstParty().getEmail())) {
            saveFirstPartySignature(contract);
            logger.info("재전송 시 제1 당사자 서명 저장: contractId={}, email={}",
                    contract.getId().getValue(), contract.getFirstParty().getEmail());
        }
    }
}
