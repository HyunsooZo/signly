package com.signly.application.contract;

import com.signly.application.contract.dto.*;
import com.signly.application.contract.mapper.ContractDtoMapper;
import com.signly.common.exception.ForbiddenException;
import com.signly.common.exception.NotFoundException;
import com.signly.common.exception.ValidationException;
import com.signly.domain.contract.model.*;
import com.signly.domain.contract.repository.ContractRepository;
import com.signly.domain.template.model.ContractTemplate;
import com.signly.domain.template.model.TemplateId;
import com.signly.domain.template.repository.TemplateRepository;
import com.signly.domain.user.model.User;
import com.signly.domain.user.model.UserId;
import com.signly.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public ContractService(ContractRepository contractRepository, UserRepository userRepository,
                         TemplateRepository templateRepository, ContractDtoMapper contractDtoMapper) {
        this.contractRepository = contractRepository;
        this.userRepository = userRepository;
        this.templateRepository = templateRepository;
        this.contractDtoMapper = contractDtoMapper;
    }

    public ContractResponse createContract(String userId, CreateContractCommand command) {
        UserId userIdObj = UserId.of(userId);
        User user = userRepository.findById(userIdObj)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        if (!user.canCreateContract()) {
            throw new ForbiddenException("계약서를 생성할 권한이 없습니다");
        }

        if (contractRepository.existsByCreatorIdAndTitle(userIdObj, command.title())) {
            throw new ValidationException("이미 같은 제목의 계약서가 존재합니다");
        }

        TemplateId templateId = command.templateId() != null ? TemplateId.of(command.templateId()) : null;
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

    public ContractResponse updateContract(String userId, String contractId, UpdateContractCommand command) {
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

    public void sendForSigning(String userId, String contractId) {
        ContractId contractIdObj = ContractId.of(contractId);
        Contract contract = contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateOwnership(userId, contract);
        contract.sendForSigning();
        contractRepository.save(contract);
    }

    public ContractResponse signContract(String signerEmail, String contractId, SignContractCommand command) {
        ContractId contractIdObj = ContractId.of(contractId);
        Contract contract = contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        contract.sign(signerEmail, command.signerName(), command.signatureData(), command.ipAddress());
        Contract savedContract = contractRepository.save(contract);
        return contractDtoMapper.toResponse(savedContract);
    }

    public void completeContract(String userId, String contractId) {
        ContractId contractIdObj = ContractId.of(contractId);
        Contract contract = contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateOwnership(userId, contract);
        contract.complete();
        contractRepository.save(contract);
    }

    public void cancelContract(String userId, String contractId) {
        ContractId contractIdObj = ContractId.of(contractId);
        Contract contract = contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateOwnership(userId, contract);
        contract.cancel();
        contractRepository.save(contract);
    }

    public void deleteContract(String userId, String contractId) {
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
    public ContractResponse getContract(String userId, String contractId) {
        ContractId contractIdObj = ContractId.of(contractId);
        Contract contract = contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        validateAccess(userId, contract);
        return contractDtoMapper.toResponse(contract);
    }

    @Transactional(readOnly = true)
    public ContractResponse getContractForSigning(String signerEmail, String contractId) {
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
    public Page<ContractResponse> getContractsByCreator(String userId, Pageable pageable) {
        UserId userIdObj = UserId.of(userId);
        Page<Contract> contracts = contractRepository.findByCreatorId(userIdObj, pageable);
        return contracts.map(contractDtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getContractsByCreatorAndStatus(String userId, ContractStatus status, Pageable pageable) {
        UserId userIdObj = UserId.of(userId);
        Page<Contract> contracts = contractRepository.findByCreatorIdAndStatus(userIdObj, status, pageable);
        return contracts.map(contractDtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getContractsByParty(String email, Pageable pageable) {
        Page<Contract> contracts = contractRepository.findByPartyEmail(email, pageable);
        return contracts.map(contractDtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getContractsByPartyAndStatus(String email, ContractStatus status, Pageable pageable) {
        Page<Contract> contracts = contractRepository.findByPartyEmailAndStatus(email, status, pageable);
        return contracts.map(contractDtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByTemplate(String userId, String templateId) {
        TemplateId templateIdObj = TemplateId.of(templateId);
        ContractTemplate template = templateRepository.findById(templateIdObj)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다"));

        validateTemplateOwnership(userId, template);

        List<Contract> contracts = contractRepository.findByTemplateId(templateIdObj);
        return contracts.stream()
                .map(contractDtoMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void expireContracts() {
        LocalDateTime now = LocalDateTime.now();
        List<Contract> expiredContracts = contractRepository.findExpiredContracts(now);

        expiredContracts.forEach(contract -> {
            contract.expire();
            contractRepository.save(contract);
        });
    }

    private void validateOwnership(String userId, Contract contract) {
        if (!contract.getCreatorId().getValue().equals(userId)) {
            throw new ForbiddenException("해당 계약서에 대한 권한이 없습니다");
        }
    }

    private void validateAccess(String userId, Contract contract) {
        if (!contract.getCreatorId().getValue().equals(userId) &&
            !contract.getFirstParty().getEmail().equals(userId) &&
            !contract.getSecondParty().getEmail().equals(userId)) {
            throw new ForbiddenException("해당 계약서에 접근할 권한이 없습니다");
        }
    }

    private void validateTemplateOwnership(String userId, ContractTemplate template) {
        if (!template.getOwnerId().getValue().equals(userId)) {
            throw new ForbiddenException("해당 템플릿에 대한 권한이 없습니다");
        }
    }
}