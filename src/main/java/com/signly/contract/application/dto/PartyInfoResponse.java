package com.signly.contract.application.dto;

public class PartyInfoResponse {
    private final String name;
    private final String email;
    private final String organizationName;

    public PartyInfoResponse(String name, String email, String organizationName) {
        this.name = name;
        this.email = email;
        this.organizationName = organizationName;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public String organizationName() {
        return organizationName;
    }
}
