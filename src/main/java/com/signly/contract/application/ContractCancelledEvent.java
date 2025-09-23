package com.signly.contract.application;

import com.signly.contract.domain.model.Contract;
import org.springframework.context.ApplicationEvent;

public class ContractCancelledEvent extends ApplicationEvent {

    private final Contract contract;
    private final String reason;

    public ContractCancelledEvent(Object source, Contract contract, String reason) {
        super(source);
        this.contract = contract;
        this.reason = reason;
    }

    public Contract getContract() {
        return contract;
    }

    public String getReason() {
        return reason;
    }
}