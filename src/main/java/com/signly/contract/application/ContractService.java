package com.signly.contract.application;

import com.signly.contract.application.dto.*;
import com.signly.common.exception.ForbiddenException;
import com.signly.common.exception.NotFoundException;
import com.signly.common.exception.ValidationException;
import com.signly.contract.application.mapper.ContractDtoMapper;
import com.signly.contract.domain.model.*;
import com.signly.contract.domain.repository.ContractRepository;
import com.signly.notification.application.EmailNotificationService;
import com.signly.signature.application.FirstPartySignatureService;
import com.signly.template.domain.model.ContractTemplate;
import com.signly.template.domain.model.TemplateId;
import com.signly.template.domain.repository.TemplateRepository;
import com.signly.user.domain.model.User;
import com.signly.user.domain.model.UserId;
import com.signly.user.domain.model.UserType;
import com.signly.user.domain.repository.UserRepository;
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

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final ContractDtoMapper contractDtoMapper;
    private final EmailNotificationService emailNotificationService;
    private final FirstPartySignatureService firstPartySignatureService;

    public ContractService(
            ContractRepository contractRepository,
            UserRepository userRepository,
            TemplateRepository templateRepository,
            ContractDtoMapper contractDtoMapper,
            EmailNotificationService emailNotificationService,
            FirstPartySignatureService firstPartySignatureService
    ) {
        this.contractRepository = contractRepository;
        this.userRepository = userRepository;
        this.templateRepository = templateRepository;
        this.contractDtoMapper = contractDtoMapper;
        this.emailNotificationService = emailNotificationService;
        this.firstPartySignatureService = firstPartySignatureService;
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

        if (contractRepository.existsByCreatorIdAndTitle(userIdObj, command.title())) {
            throw new ValidationException("이미 같은 제목의 계약서가 존재합니다");
        }

        TemplateId templateId = StringUtils.hasText(command.templateId()) ? TemplateId.of(command.templateId().trim()) : null;
        if (templateId != null) {
            ContractTemplate template = templateRepository.findById(templateId)
                    .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다"));

            if (!template.getOwnerId().equals(userIdObj)) {
                throw new ForbiddenException("해당 템플릿을 사용할 권한이 없습니다");
            }
        }

        ContractContent content = ContractContent.of(command.content());
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
                command.expiresAt()
        );

        Contract savedContract = contractRepository.save(contract);
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

        if (!command.title().equals(contract.getTitle()) &&
                contractRepository.existsByCreatorIdAndTitle(contract.getCreatorId(), command.title())) {
            throw new ValidationException("이미 같은 제목의 계약서가 존재합니다");
        }

        contract.updateTitle(command.title());
        ContractContent newContent = ContractContent.of(command.content());
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
        firstPartySignatureService.ensureSignatureExists(contract.getCreatorId().getValue());
        contract.sendForSigning();
        Contract savedContract = contractRepository.save(contract);
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

        firstPartySignatureService.ensureSignatureExists(contract.getCreatorId().getValue());
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
        SignToken signToken = SignToken.of(token);
        Contract contract = contractRepository.findBySignToken(signToken)
                .orElseThrow(() -> new NotFoundException("유효하지 않은 서명 링크입니다"));

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

        contract.sign(signerEmail, signerName, signatureData, ipAddress);
        Contract savedContract = contractRepository.save(contract);
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
}
