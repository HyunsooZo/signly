package com.deally.contract.application;

import com.deally.contract.domain.model.Contract;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ContractCompletedEvent extends ApplicationEvent {

    private final Contract contract;

    public ContractCompletedEvent(
            Object source,
            Contract contract
    ) {
        super(source);
        this.contract = contract;
    }

}