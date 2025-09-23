package com.signly.application.event;

import com.signly.domain.contract.model.Contract;
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