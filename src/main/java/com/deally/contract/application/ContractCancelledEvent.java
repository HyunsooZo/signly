package com.deally.contract.application;

import com.deally.contract.domain.model.Contract;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ContractCancelledEvent extends ApplicationEvent {

    private final Contract contract;
    private final String reason;

    public ContractCancelledEvent(
            Object source,
            Contract contract,
            String reason
    ) {
        super(source);
        this.contract = contract;
        this.reason = reason;
    }

}