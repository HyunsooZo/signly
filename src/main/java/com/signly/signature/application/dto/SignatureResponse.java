package com.signly.signature.application.dto;

import java.time.LocalDateTime;

public record SignatureResponse(
        String signatureId,
        String contractId,
        String signerEmail,
        String signerName,
        LocalDateTime signedAt,
        String ipAddress,
        String deviceInfo
) {
}