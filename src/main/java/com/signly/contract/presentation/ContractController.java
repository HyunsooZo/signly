package com.signly.contract.presentation;

import com.signly.contract.application.ContractService;
import com.signly.contract.application.dto.*;
import com.signly.contract.domain.model.ContractStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping
    public ResponseEntity<ContractResponse> createContract(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateContractCommand command) {
        ContractResponse response = contractService.createContract(userId, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{contractId}")
    public ResponseEntity<ContractResponse> updateContract(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String contractId,
            @Valid @RequestBody UpdateContractCommand command) {
        ContractResponse response = contractService.updateContract(userId, contractId, command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{contractId}/send")
    public ResponseEntity<Void> sendForSigning(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String contractId) {
        contractService.sendForSigning(userId, contractId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{contractId}/sign")
    public ResponseEntity<ContractResponse> signContract(
            @RequestHeader("X-Signer-Email") String signerEmail,
            @PathVariable String contractId,
            @Valid @RequestBody SignContractCommand command,
            HttpServletRequest request) {

        String ipAddress = getClientIpAddress(request);
        SignContractCommand commandWithIp = new SignContractCommand(
            command.signerName(),
            command.signatureData(),
            ipAddress
        );

        ContractResponse response = contractService.signContract(signerEmail, contractId, commandWithIp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{contractId}/complete")
    public ResponseEntity<Void> completeContract(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String contractId) {
        contractService.completeContract(userId, contractId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{contractId}/cancel")
    public ResponseEntity<Void> cancelContract(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String contractId) {
        contractService.cancelContract(userId, contractId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{contractId}")
    public ResponseEntity<Void> deleteContract(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String contractId) {
        contractService.deleteContract(userId, contractId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{contractId}")
    public ResponseEntity<ContractResponse> getContract(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String contractId) {
        ContractResponse response = contractService.getContract(userId, contractId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{contractId}/signing")
    public ResponseEntity<ContractResponse> getContractForSigning(
            @RequestHeader("X-Signer-Email") String signerEmail,
            @PathVariable String contractId) {
        ContractResponse response = contractService.getContractForSigning(signerEmail, contractId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ContractResponse>> getMyContracts(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<ContractResponse> response = contractService.getContractsByCreator(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my/status/{status}")
    public ResponseEntity<Page<ContractResponse>> getMyContractsByStatus(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable ContractStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<ContractResponse> response = contractService.getContractsByCreatorAndStatus(userId, status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/party")
    public ResponseEntity<Page<ContractResponse>> getContractsByParty(
            @RequestHeader("X-User-Email") String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<ContractResponse> response = contractService.getContractsByParty(email, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/party/status/{status}")
    public ResponseEntity<Page<ContractResponse>> getContractsByPartyAndStatus(
            @RequestHeader("X-User-Email") String email,
            @PathVariable ContractStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<ContractResponse> response = contractService.getContractsByPartyAndStatus(email, status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<ContractResponse>> getContractsByTemplate(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String templateId) {
        List<ContractResponse> response = contractService.getContractsByTemplate(userId, templateId);
        return ResponseEntity.ok(response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }
}