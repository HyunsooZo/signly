package com.signly.signature.application;

import com.signly.common.exception.BusinessException;
import com.signly.common.exception.NotFoundException;
import com.signly.common.exception.ValidationException;
import com.signly.contract.domain.model.ContractId;
import com.signly.signature.application.dto.CreateSignatureCommand;
import com.signly.signature.application.dto.SignatureResponse;
import com.signly.signature.application.mapper.SignatureDtoMapper;
import com.signly.signature.domain.model.ContractSignature;
import com.signly.signature.domain.model.SignatureId;
import com.signly.signature.domain.repository.SignatureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SignatureService {

    private static final Logger logger = LoggerFactory.getLogger(SignatureService.class);

    private final SignatureRepository signatureRepository;
    private final SignatureDtoMapper mapper;

    public SignatureService(SignatureRepository signatureRepository, SignatureDtoMapper mapper) {
        this.signatureRepository = signatureRepository;
        this.mapper = mapper;
    }

    public SignatureResponse createSignature(CreateSignatureCommand command) {
        logger.info("서명 생성 시작: contractId={}, signerEmail={}", command.contractId(), command.signerEmail());

        ContractId contractId = ContractId.of(command.contractId());

        if (signatureRepository.existsByContractIdAndSignerEmail(contractId, command.signerEmail())) {
            throw new BusinessException("이미 서명된 계약서입니다");
        }

        ContractSignature signature = ContractSignature.create(
                contractId,
                command.signatureData(),
                command.signerEmail(),
                command.signerName(),
                command.ipAddress(),
                command.deviceInfo()
        );

        if (!signature.validate()) {
            throw new ValidationException("서명 데이터가 유효하지 않습니다");
        }

        signatureRepository.save(signature);

        logger.info("서명 생성 완료: signatureId={}", signature.id().value());
        return mapper.toResponse(signature);
    }

    @Transactional(readOnly = true)
    public SignatureResponse getSignature(String signatureId) {
        ContractSignature signature = signatureRepository.findById(SignatureId.of(signatureId))
                .orElseThrow(() -> new NotFoundException("서명을 찾을 수 없습니다: " + signatureId));

        return mapper.toResponse(signature);
    }

    @Transactional(readOnly = true)
    public List<SignatureResponse> getSignaturesByContract(String contractId) {
        ContractId cId = ContractId.of(contractId);
        List<ContractSignature> signatures = signatureRepository.findByContractId(cId);

        return signatures.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isContractSigned(String contractId, String signerEmail) {
        ContractId cId = ContractId.of(contractId);
        boolean exists = signatureRepository.existsByContractIdAndSignerEmail(cId, signerEmail);
        logger.info("서명 여부 체크: contractId={}, signerEmail={}, exists={}", contractId, signerEmail, exists);
        return exists;
    }

    @Transactional(readOnly = true)
    public SignatureResponse getContractSignature(String contractId, String signerEmail) {
        ContractId cId = ContractId.of(contractId);
        ContractSignature signature = signatureRepository.findByContractIdAndSignerEmail(cId, signerEmail)
                .orElseThrow(() -> new NotFoundException("서명을 찾을 수 없습니다"));

        return mapper.toResponse(signature);
    }

    public void deleteSignature(String signatureId) {
        SignatureId sId = SignatureId.of(signatureId);

        if (!signatureRepository.findById(sId).isPresent()) {
            throw new NotFoundException("서명을 찾을 수 없습니다: " + signatureId);
        }

        signatureRepository.delete(sId);
        logger.info("서명 삭제 완료: signatureId={}", signatureId);
    }
}