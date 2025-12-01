package com.deally.contract.application;

import com.deally.contract.domain.model.Contract;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ContractSignedEvent extends ApplicationEvent {

    private final Contract contract;
    private final String signerName;

    public ContractSignedEvent(
            Object source,
            Contract contract,
            String signerName
    ) {
        super(source);
        this.contract = contract;
        this.signerName = signerName;
    }

}