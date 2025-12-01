package com.deally.contract.application.dto;

public record SignContractCommand(
        String signerName,
        String signatureData,
        String ipAddress
) {}