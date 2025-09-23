package com.signly.contract.application.dto;

import java.time.LocalDateTime;

public record SignatureResponse(
    String signerEmail,
    String signerName,
    LocalDateTime signedAt,
    String ipAddress
) {}