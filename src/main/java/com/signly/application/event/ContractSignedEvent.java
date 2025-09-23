package com.signly.application.event;

import com.signly.domain.contract.model.Contract;
import org.springframework.context.ApplicationEvent;

public class ContractSignedEvent extends ApplicationEvent {

    private final Contract contract;
    private final String signerName;

    public ContractSignedEvent(Object source, Contract contract, String signerName) {
        super(source);
        this.contract = contract;
        this.signerName = signerName;
    }

    public Contract getContract() {
        return contract;
    }

    public String getSignerName() {
        return signerName;
    }
}