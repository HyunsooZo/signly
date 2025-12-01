package com.deally.contract.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSignatureCommand(
        @NotBlank(message = "계약서 ID는 필수입니다")
        String contractId,

        @NotBlank(message = "서명 데이터는 필수입니다")
        String signatureData,

        @NotBlank(message = "서명자 이메일은 필수입니다")
        String signerEmail,

        @NotBlank(message = "서명자 이름은 필수입니다")
        String signerName,

        String ipAddress,
        String deviceInfo
) {
}