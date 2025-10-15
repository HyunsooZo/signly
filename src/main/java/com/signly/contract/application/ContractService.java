package com.signly.contract.application;

import com.signly.contract.application.dto.*;
import com.signly.common.exception.ForbiddenException;
import com.signly.common.exception.NotFoundException;
import com.signly.common.exception.ValidationException;
import com.signly.contract.application.mapper.ContractDtoMapper;
import com.signly.contract.application.support.ContractHtmlSanitizer;
import com.signly.contract.domain.model.*;
import com.signly.contract.domain.repository.ContractRepository;
import com.signly.notification.application.EmailNotificationService;
import com.signly.signature.application.FirstPartySignatureService;
import com.signly.signature.application.SignatureService;
import com.signly.signature.application.dto.CreateSignatureCommand;
import com.signly.signature.domain.repository.SignatureRepository;
import com.signly.template.domain.model.ContractTemplate;
import com.signly.template.domain.model.TemplateId;
import com.signly.template.domain.repository.TemplateRepository;
import com.signly.user.domain.model.User;
import com.signly.user.domain.model.UserId;
import com.signly.user.domain.model.UserType;
import com.signly.user.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContractService {

    private final Logger logger = LoggerFactory.getLogger(ContractService.class);

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final ContractDtoMapper contractDtoMapper;
    private final EmailNotificationService emailNotificationService;
    private final FirstPartySignatureService firstPartySignatureService;
    private final SignatureRepository signatureRepository;
    private final SignatureService signatureService;

    public ContractService(
            ContractRepository contractRepository,
            UserRepository userRepository,
            TemplateRepository templateRepository,
            ContractDtoMapper contractDtoMapper,
            EmailNotificationService emailNotificationService,
            FirstPartySignatureService firstPartySignatureService,
            SignatureRepository signatureRepository,
            SignatureService signatureService
    ) {
        this.contractRepository = contractRepository;
        this.userRepository = userRepository;
        this.templateRepository = templateRepository;
        this.contractDtoMapper = contractDtoMapper;
        this.emailNotificationService = emailNotificationService;
        this.firstPartySignatureService = firstPartySignatureService;
        this.signatureRepository = signatureRepository;
        this.signatureService = signatureService;
    }

    public ContractResponse createContract(
            String userId,
            CreateContractCommand command
    ) {
        UserId userIdObj = UserId.of(userId);
        User user = userRepository.findById(userIdObj)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        if (!user.canCreateContract()) {
            throw new ForbiddenException("계약서를 생성할 권한이 없습니다");
        }

        if (user.getUserType() == UserType.OWNER) {
            firstPartySignatureService.ensureSignatureExists(userId);
        }

        TemplateId templateId = StringUtils.hasText(command.templateId()) ? TemplateId.of(command.templateId().trim()) : null;
        if (templateId != null) {
            ContractTemplate template = templateRepository.findById(templateId)
                    .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다"));

            if (!template.getOwnerId().equals(userIdObj)) {
                throw new ForbiddenException("해당 템플릿을 사용할 권한이 없습니다");
            }
        }

        String sanitizedContent = ContractHtmlSanitizer.sanitize(command.content());
        ContractContent content = ContractContent.of(sanitizedContent);
        PartyInfo firstParty = PartyInfo.of(
                command.firstPartyName(),
                command.firstPartyEmail(),
                command.firstPartyOrganization()
        );
        PartyInfo secondParty = PartyInfo.of(
                command.secondPartyName(),
                command.secondPartyEmail(),
                command.secondPartyOrganization()
        );

        Contract contract = Contract.create(
                userIdObj,
                templateId,
                command.title(),
                content,
                firstParty,
                secondParty,
                command.expiresAt(),
                command.presetType()
        );

        Contract savedContract = contractRepository.save(contract);
        logger.info("계약서 생성 완료: contractId={}, signToken={}",
            savedContract.getId().getValue(), savedContract.getSignToken().value());
        return contractDtoMapper.toResponse(savedContract);
    }

    public ContractResponse updateContract(
            String userId,
            String contractId,
            UpdateContractCommand command
    ) {
        ContractId contractIdObj = ContractId.of(contractId);
        Contract contract = contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateOwnership(userId, contract);

        contract.updateTitle(command.title());
        String sanitizedContent = ContractHtmlSanitizer.sanitize(command.content());
        ContractContent newContent = ContractContent.of(sanitizedContent);
        contract.updateContent(newContent);

        if (command.expiresAt() != null) {
            contract.updateExpirationDate(command.expiresAt());
        }

        Contract updatedContract = contractRepository.save(contract);
        return contractDtoMapper.toResponse(updatedContract);
    }

    public void sendForSigning(
            String userId,
            String contractId
    ) {
        ContractId contractIdObj = ContractId.of(contractId);
        Contract contract = contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateOwnership(userId, contract);

        // 1. 제1 당사자(발송자)의 서명 이미지가 등록되어 있는지 확인
        firstPartySignatureService.ensureSignatureExists(contract.getCreatorId().getValue());

        // 2. 제1 당사자의 서명을 contract_signatures 테이블에 저장
        String firstPartySignatureData = firstPartySignatureService.getSignatureDataUrl(contract.getCreatorId().getValue());
        CreateSignatureCommand firstPartyCommand = new CreateSignatureCommand(
                contract.getId().getValue(),
                firstPartySignatureData,
                contract.getFirstParty().getEmail(),
                contract.getFirstParty().getName(),
                "SERVER", // 서버에서 발송하므로
                "Server-initiated signature" // 서버에서 자동 서명
        );
        signatureService.createSignature(firstPartyCommand);
        logger.info("제1 당사자 서명 저장 완료: contractId={}, email={}",
                contract.getId().getValue(), contract.getFirstParty().getEmail());

        // 3. 계약서 상태를 PENDING으로 변경
        contract.sendForSigning();
        Contract savedContract = contractRepository.save(contract);

        // 4. 제2 당사자에게 서명 요청 이메일 발송
        emailNotificationService.sendContractSigningRequest(savedContract);
    }

    public void resendSigningEmail(
            String userId,
            String contractId
    ) {
        ContractId contractIdObj = ContractId.of(contractId);
        Contract contract = contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateOwnership(userId, contract);

        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new ValidationException("서명 대기 상태에서만 서명 요청을 재전송할 수 있습니다");
        }

        // 제1 당사자 서명이 있는지 확인하고, 없으면 저장 (기존 데이터 보정)
        firstPartySignatureService.ensureSignatureExists(contract.getCreatorId().getValue());
        ContractId cId = contract.getId();
        if (!signatureRepository.existsByContractIdAndSignerEmail(cId, contract.getFirstParty().getEmail())) {
            String firstPartySignatureData = firstPartySignatureService.getSignatureDataUrl(contract.getCreatorId().getValue());
            CreateSignatureCommand firstPartyCommand = new CreateSignatureCommand(
                    contract.getId().getValue(),
                    firstPartySignatureData,
                    contract.getFirstParty().getEmail(),
                    contract.getFirstParty().getName(),
                    "SERVER",
                    "Server-initiated signature on resend"
            );
            signatureService.createSignature(firstPartyCommand);
            logger.info("재전송 시 제1 당사자 서명 저장: contractId={}, email={}",
                    contract.getId().getValue(), contract.getFirstParty().getEmail());
        }

        emailNotificationService.sendContractSigningRequest(contract);
    }

    public ContractResponse signContract(
            String signerEmail,
            String contractId,
            SignContractCommand command
    ) {
        ContractId contractIdObj = ContractId.of(contractId);
        Contract contract = contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        contract.sign(signerEmail, command.signerName(), command.signatureData(), command.ipAddress());
        Contract savedContract = contractRepository.save(contract);
        return contractDtoMapper.toResponse(savedContract);
    }

    public void completeContract(
            String userId,
            String contractId
    ) {
        ContractId contractIdObj = ContractId.of(contractId);
        Contract contract = contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateOwnership(userId, contract);
        firstPartySignatureService.ensureSignatureExists(contract.getCreatorId().getValue());
        contract.complete();
        Contract savedContract = contractRepository.save(contract);
        emailNotificationService.sendContractCompleted(savedContract);
    }

    public void cancelContract(
            String userId,
            String contractId
    ) {
        ContractId contractIdObj = ContractId.of(contractId);
        Contract contract = contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateOwnership(userId, contract);
        contract.cancel();
        Contract savedContract = contractRepository.save(contract);
        emailNotificationService.sendContractCancelled(savedContract);
    }

    public void deleteContract(
            String userId,
            String contractId
    ) {
        ContractId contractIdObj = ContractId.of(contractId);
        Contract contract = contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateOwnership(userId, contract);

        if (!contract.canDelete()) {
            throw new ValidationException("DRAFT 상태의 계약서만 삭제할 수 있습니다");
        }

        contractRepository.delete(contract);
    }

    @Transactional(readOnly = true)
    public ContractResponse getContract(
            String userId,
            String contractId
    ) {
        ContractId contractIdObj = ContractId.of(contractId);
        Contract contract = contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateAccess(userId, contract);
        return contractDtoMapper.toResponse(contract);
    }

    @Transactional(readOnly = true)
    public ContractResponse getContractForSigning(
            String signerEmail,
            String contractId
    ) {
        ContractId contractIdObj = ContractId.of(contractId);
        Contract contract = contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        if (!contract.getFirstParty().getEmail().equals(signerEmail) &&
                !contract.getSecondParty().getEmail().equals(signerEmail)) {
            throw new ForbiddenException("해당 계약서에 접근할 권한이 없습니다");
        }

        return contractDtoMapper.toResponse(contract);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getContractsByCreator(
            String userId,
            Pageable pageable
    ) {
        UserId userIdObj = UserId.of(userId);
        Page<Contract> contracts = contractRepository.findByCreatorId(userIdObj, pageable);
        return contracts.map(contractDtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getContractsByCreatorAndStatus(
            String userId,
            ContractStatus status,
            Pageable pageable
    ) {
        UserId userIdObj = UserId.of(userId);
        Page<Contract> contracts = contractRepository.findByCreatorIdAndStatus(userIdObj, status, pageable);
        return contracts.map(contractDtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getContractsByParty(
            String email,
            Pageable pageable
    ) {
        Page<Contract> contracts = contractRepository.findByPartyEmail(email, pageable);
        return contracts.map(contractDtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getContractsByPartyAndStatus(
            String email,
            ContractStatus status,
            Pageable pageable
    ) {
        Page<Contract> contracts = contractRepository.findByPartyEmailAndStatus(email, status, pageable);
        return contracts.map(contractDtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByTemplate(
            String userId,
            String templateId
    ) {
        TemplateId templateIdObj = TemplateId.of(templateId);
        ContractTemplate template = templateRepository.findById(templateIdObj)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다"));

        validateTemplateOwnership(userId, template);

        List<Contract> contracts = contractRepository.findByTemplateId(templateIdObj);
        return contracts.stream()
                .map(contractDtoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContractResponse getContractByToken(String token) {
        logger.info("토큰으로 계약서 조회: token={}", token);
        SignToken signToken = SignToken.of(token);
        Contract contract = contractRepository.findBySignToken(signToken)
                .orElseThrow(() -> {
                    logger.error("서명 토큰으로 계약서를 찾을 수 없음: token={}", token);
                    return new NotFoundException("유효하지 않은 서명 링크입니다");
                });

        logger.info("계약서 찾음: contractId={}, status={}", contract.getId().getValue(), contract.getStatus());

        if (contract.isExpired()) {
            throw new ValidationException("만료된 계약서입니다");
        }

        return contractDtoMapper.toResponse(contract);
    }

    public ContractResponse processSignature(
            String token,
            String signerEmail,
            String signerName,
            String signatureData,
            String ipAddress
    ) {
        SignToken signToken = SignToken.of(token);
        Contract contract = contractRepository.findBySignToken(signToken)
                .orElseThrow(() -> new NotFoundException("유효하지 않은 서명 링크입니다"));

        // 모든 당사자가 서명을 완료했는지 확인 (contract_signatures 테이블에서 확인)
        ContractId contractId = contract.getId();
        boolean firstPartySigned = signatureRepository.existsByContractIdAndSignerEmail(
                contractId, normalizeEmail(contract.getFirstParty().getEmail()));
        boolean secondPartySigned = signatureRepository.existsByContractIdAndSignerEmail(
                contractId, normalizeEmail(contract.getSecondParty().getEmail()));

        // 현재 서명하는 사람이 어느 당사자인지 확인하여 서명 후 상태 결정
        boolean allSignaturesComplete;
        if (normalizeEmail(signerEmail).equals(normalizeEmail(contract.getFirstParty().getEmail()))) {
            // firstParty가 서명하는 경우, secondParty도 서명했으면 완료
            allSignaturesComplete = secondPartySigned;
        } else {
            // secondParty가 서명하는 경우, firstParty도 서명했으면 완료
            allSignaturesComplete = firstPartySigned;
        }

        // 서명 검증 및 상태 업데이트 (서명 데이터는 SignatureService에서 별도 저장)
        contract.markSignedBy(normalizeEmail(signerEmail), allSignaturesComplete);
        Contract savedContract = contractRepository.save(contract);

        // 모든 당사자의 서명이 완료되면 양측에 완료 이메일 발송
        if (allSignaturesComplete) {
            logger.info("모든 서명 완료, 완료 알림 이메일 발송: contractId={}", contract.getId().getValue());
            emailNotificationService.sendContractCompleted(savedContract);
        }

        return contractDtoMapper.toResponse(savedContract);
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

    private void validateOwnership(
            String userId,
            Contract contract
    ) {
        if (!contract.getCreatorId().getValue().equals(userId)) {
            throw new ForbiddenException("해당 계약서에 대한 권한이 없습니다");
        }
    }

    private void validateAccess(
            String userId,
            Contract contract
    ) {
        if (!contract.getCreatorId().getValue().equals(userId) &&
                !contract.getFirstParty().getEmail().equals(userId) &&
                !contract.getSecondParty().getEmail().equals(userId)) {
            throw new ForbiddenException("해당 계약서에 접근할 권한이 없습니다");
        }
    }

    private void validateTemplateOwnership(
            String userId,
            ContractTemplate template
    ) {
        if (!template.getOwnerId().getValue().equals(userId)) {
            throw new ForbiddenException("해당 템플릿에 대한 권한이 없습니다");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
