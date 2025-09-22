package com.signly.application.contract.dto;

import java.time.LocalDateTime;

public record SignatureResponse(
    String signerEmail,
    String signerName,
    LocalDateTime signedAt,
    String ipAddress
) {}