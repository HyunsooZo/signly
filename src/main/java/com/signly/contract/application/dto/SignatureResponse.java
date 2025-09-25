package com.signly.contract.application.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Getter
@AllArgsConstructor
public class SignatureResponse {
    private final String signerEmail;
    private final String signerName;
    @Getter(AccessLevel.NONE)
    private final LocalDateTime signedAt;
    private final String ipAddress;

    public Date getSignedAt() {
        if (signedAt == null) {
            return null;
        }
        return Date.from(signedAt.atZone(ZoneId.systemDefault()).toInstant());
    }

    public LocalDateTime getSignedAtLocalDateTime() {
        return signedAt;
    }
}
