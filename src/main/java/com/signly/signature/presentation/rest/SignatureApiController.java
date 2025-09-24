package com.signly.signature.presentation.rest;

import com.signly.signature.application.SignatureService;
import com.signly.signature.application.dto.CreateSignatureCommand;
import com.signly.signature.application.dto.SignatureResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/signatures")
public class SignatureApiController {

    private static final Logger logger = LoggerFactory.getLogger(SignatureApiController.class);
    private final SignatureService signatureService;

    public SignatureApiController(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @PostMapping
    public ResponseEntity<SignatureResponse> createSignature(
            @Valid @RequestBody CreateSignatureCommand command,
            HttpServletRequest request) {

        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        CreateSignatureCommand enrichedCommand = new CreateSignatureCommand(
                command.contractId(),
                command.signatureData(),
                command.signerEmail(),
                command.signerName(),
                ipAddress,
                userAgent
        );

        SignatureResponse response = signatureService.createSignature(enrichedCommand);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{signatureId}")
    public ResponseEntity<SignatureResponse> getSignature(@PathVariable String signatureId) {
        SignatureResponse response = signatureService.getSignature(signatureId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SignatureResponse>> getSignaturesByContract(
            @RequestParam String contractId) {
        List<SignatureResponse> responses = signatureService.getSignaturesByContract(contractId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/contract/{contractId}/signer/{signerEmail}")
    public ResponseEntity<SignatureResponse> getContractSignature(
            @PathVariable String contractId,
            @PathVariable String signerEmail) {
        SignatureResponse response = signatureService.getContractSignature(contractId, signerEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contract/{contractId}/signer/{signerEmail}/exists")
    public ResponseEntity<Boolean> isContractSigned(
            @PathVariable String contractId,
            @PathVariable String signerEmail) {
        boolean exists = signatureService.isContractSigned(contractId, signerEmail);
        return ResponseEntity.ok(exists);
    }

    @DeleteMapping("/{signatureId}")
    public ResponseEntity<Void> deleteSignature(@PathVariable String signatureId) {
        signatureService.deleteSignature(signatureId);
        return ResponseEntity.noContent().build();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}