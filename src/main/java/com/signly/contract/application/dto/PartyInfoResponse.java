package com.signly.contract.application.dto;

public record PartyInfoResponse(
    String name,
    String email,
    String organizationName
) {}