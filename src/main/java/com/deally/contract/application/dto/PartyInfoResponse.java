package com.deally.contract.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PartyInfoResponse {
    private final String name;
    private final String email;
    private final String organizationName;
}
