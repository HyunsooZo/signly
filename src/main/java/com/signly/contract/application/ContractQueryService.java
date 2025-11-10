package com.signly.contract.application;

import com.signly.common.exception.NotFoundException;
import com.signly.common.exception.ValidationException;
import com.signly.contract.application.dto.ContractResponse;
import com.signly.contract.application.mapper.ContractDtoMapper;
import com.signly.contract.domain.model.Contract;
import com.signly.contract.domain.model.ContractId;
import com.signly.contract.domain.model.ContractStatus;
import com.signly.contract.domain.model.SignToken;
import com.signly.contract.domain.repository.ContractRepository;
import com.signly.template.domain.model.TemplateId;
import com.signly.user.domain.model.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 계약서 조회 서비스
 * SRP: 읽기 전용 조회 책임만 담당
 */
@Service
@Transactional(readOnly = true)
public class ContractQueryService {

    private static final Logger logger = LoggerFactory.getLogger(ContractQueryService.class);

    private final ContractRepository contractRepository;
    private final ContractDtoMapper contractDtoMapper;

    public ContractQueryService(ContractRepository contractRepository, ContractDtoMapper contractDtoMapper) {
        this.contractRepository = contractRepository;
        this.contractDtoMapper = contractDtoMapper;
    }

    public Contract findById(String contractId) {
        ContractId contractIdObj = ContractId.of(contractId);
        return contractRepository.findById(contractIdObj)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));
    }

    public ContractResponse getContract(String contractId) {
        Contract contract = findById(contractId);
        return contractDtoMapper.toResponse(contract);
    }

    public Page<ContractResponse> getContractsByCreator(String userId, Pageable pageable) {
        UserId userIdObj = UserId.of(userId);
        Page<Contract> contracts = contractRepository.findByCreatorId(userIdObj, pageable);
        return contracts.map(contractDtoMapper::toResponse);
    }

    public Page<ContractResponse> getContractsByCreatorAndStatus(
            String userId,
            ContractStatus status,
            Pageable pageable
    ) {
        UserId userIdObj = UserId.of(userId);
        Page<Contract> contracts = contractRepository.findByCreatorIdAndStatus(userIdObj, status, pageable);
        return contracts.map(contractDtoMapper::toResponse);
    }

    public Page<ContractResponse> getContractsByParty(String email, Pageable pageable) {
        Page<Contract> contracts = contractRepository.findByPartyEmail(email, pageable);
        return contracts.map(contractDtoMapper::toResponse);
    }

    public Page<ContractResponse> getContractsByPartyAndStatus(
            String email,
            ContractStatus status,
            Pageable pageable
    ) {
        Page<Contract> contracts = contractRepository.findByPartyEmailAndStatus(email, status, pageable);
        return contracts.map(contractDtoMapper::toResponse);
    }

    public List<ContractResponse> getContractsByTemplate(String templateId) {
        TemplateId templateIdObj = TemplateId.of(templateId);
        List<Contract> contracts = contractRepository.findByTemplateId(templateIdObj);
        return contracts.stream()
                .map(contractDtoMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ContractResponse getContractByToken(String token) {
        logger.info("토큰으로 계약서 조회: token={}", token);
        SignToken signToken = SignToken.of(token);
        Contract contract = contractRepository.findBySignToken(signToken)
                .orElseThrow(() -> {
                    logger.error("서명 토큰으로 계약서를 찾을 수 없음: token={}", token);
                    return new NotFoundException("유효하지 않은 서명 링크입니다");
                });

        logger.info("계약서 찾음: contractId={}, status={}", contract.getId().value(), contract.getStatus());

        if (contract.isExpired()) {
            throw new ValidationException("만료된 계약서입니다");
        }

        return contractDtoMapper.toResponse(contract);
    }

    public Contract findByToken(String token) {
        SignToken signToken = SignToken.of(token);
        return contractRepository.findBySignToken(signToken)
                .orElseThrow(() -> new NotFoundException("유효하지 않은 서명 링크입니다"));
    }
}
