package com.signly.application.contract.dto;

import java.time.LocalDateTime;

public record UpdateContractCommand(
    String title,
    String content,
    LocalDateTime expiresAt
) {}