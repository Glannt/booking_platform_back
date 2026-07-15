package com.dotnt.booking_travel_backend.shared.module;

import org.springframework.context.ApplicationEvent;

import java.util.concurrent.CompletableFuture;

public class ModuleRequestEvent extends ApplicationEvent {
    private final String requestType;
    private final Object payload;
    @SuppressWarnings("rawtypes")
    private final CompletableFuture future;

    @SuppressWarnings("rawtypes")
    public ModuleRequestEvent(Object source, String requestType, Object payload, CompletableFuture future) {
        super(source);
        this.requestType = requestType;
        this.payload = payload;
        this.future = future;
    }

    public String getRequestType() {
        return requestType;
    }

    public Object getPayload() {
        return payload;
    }

    @SuppressWarnings("rawtypes")
    public CompletableFuture getFuture() {
        return future;
    }
}

