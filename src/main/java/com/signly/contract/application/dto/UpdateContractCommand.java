package com.signly.contract.application.dto;

import java.time.LocalDateTime;

public record UpdateContractCommand(
        String title,
        String content,
        LocalDateTime expiresAt
) {}