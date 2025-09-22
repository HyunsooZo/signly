package com.signly.application.contract.dto;

public record SignContractCommand(
    String signerName,
    String signatureData,
    String ipAddress
) {}