package com.signly.template.application;

import org.springframework.context.ApplicationEvent;

public class VariableCacheRefreshEvent extends ApplicationEvent {
    
    public VariableCacheRefreshEvent(Object source) {
        super(source);
    }
}
