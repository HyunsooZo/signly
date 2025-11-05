package com.signly.contract.application;

import com.signly.contract.application.dto.*;
import com.signly.contract.application.mapper.ContractDtoMapper;
import com.signly.contract.domain.model.Contract;
import com.signly.contract.domain.model.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 계약서 서비스 Facade
 * 기존 API를 유지하면서 내부적으로 책임별로 분리된 서비스들에 위임
 */
@Service
@Transactional
public class ContractService {

    private final ContractCreationService creationService;
    private final ContractSigningCoordinator signingCoordinator;
    private final ContractQueryService queryService;
    private final ContractAuthorizationService authorizationService;
    private final ContractDtoMapper contractDtoMapper;

    public ContractService(
            ContractCreationService creationService,
            ContractSigningCoordinator signingCoordinator,
            ContractQueryService queryService,
            ContractAuthorizationService authorizationService,
            ContractDtoMapper contractDtoMapper
    ) {
        this.creationService = creationService;
        this.signingCoordinator = signingCoordinator;
        this.queryService = queryService;
        this.authorizationService = authorizationService;
        this.contractDtoMapper = contractDtoMapper;
    }

    // ========== 생성/수정/삭제 ==========

    public ContractResponse createContract(String userId, CreateContractCommand command) {
        Contract contract = creationService.createContract(userId, command);
        return contractDtoMapper.toResponse(contract);
    }

    public ContractResponse updateContract(String userId, String contractId, UpdateContractCommand command) {
        Contract contract = creationService.updateContract(userId, contractId, command);
        return contractDtoMapper.toResponse(contract);
    }

    public void deleteContract(String userId, String contractId) {
        creationService.deleteContract(userId, contractId);
    }

    // ========== 서명 프로세스 ==========

    public void sendForSigning(String userId, String contractId) {
        signingCoordinator.sendForSigning(userId, contractId);
    }

    public void resendSigningEmail(String userId, String contractId) {
        signingCoordinator.resendSigningEmail(userId, contractId);
    }

    public ContractResponse signContract(String signerEmail, String contractId, SignContractCommand command) {
        Contract contract = signingCoordinator.signContract(signerEmail, contractId, command);
        return contractDtoMapper.toResponse(contract);
    }

    public ContractResponse processSignature(
            String token,
            String signerEmail,
            String signerName,
            String signatureData,
            String ipAddress
    ) {
        Contract contract = signingCoordinator.processSignature(token, signerEmail, signerName, signatureData, ipAddress);
        return contractDtoMapper.toResponse(contract);
    }

    public void completeContract(String userId, String contractId) {
        signingCoordinator.completeContract(userId, contractId);
    }

    public void cancelContract(String userId, String contractId) {
        signingCoordinator.cancelContract(userId, contractId);
    }

    public void expireContracts() {
        signingCoordinator.expireContracts();
    }

    // ========== 조회 ==========

    @Transactional(readOnly = true)
    public ContractResponse getContract(String userId, String contractId) {
        Contract contract = queryService.findById(contractId);
        authorizationService.validateAccess(userId, contract);
        return contractDtoMapper.toResponse(contract);
    }

    @Transactional(readOnly = true)
    public ContractResponse getContractForSigning(String signerEmail, String contractId) {
        Contract contract = queryService.findById(contractId);
        authorizationService.validateSigningAccess(signerEmail, contract);
        return contractDtoMapper.toResponse(contract);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getContractsByCreator(String userId, Pageable pageable) {
        return queryService.getContractsByCreator(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getContractsByCreatorAndStatus(
            String userId,
            ContractStatus status,
            Pageable pageable
    ) {
        return queryService.getContractsByCreatorAndStatus(userId, status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getContractsByParty(String email, Pageable pageable) {
        return queryService.getContractsByParty(email, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getContractsByPartyAndStatus(
            String email,
            ContractStatus status,
            Pageable pageable
    ) {
        return queryService.getContractsByPartyAndStatus(email, status, pageable);
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByTemplate(String userId, String templateId) {
        return queryService.getContractsByTemplate(templateId);
    }

    @Transactional(readOnly = true)
    public ContractResponse getContractByToken(String token) {
        return queryService.getContractByToken(token);
    }
}
