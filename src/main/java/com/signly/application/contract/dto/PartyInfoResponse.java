package com.signly.application.contract.dto;

public record PartyInfoResponse(
    String name,
    String email,
    String organizationName
) {}