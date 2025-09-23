package com.signly.contract.application;

import com.signly.contract.domain.model.Contract;
import org.springframework.context.ApplicationEvent;

public class ContractSentForSigningEvent extends ApplicationEvent {

    private final Contract contract;

    public ContractSentForSigningEvent(Object source, Contract contract) {
        super(source);
        this.contract = contract;
    }

    public Contract getContract() {
        return contract;
    }
}