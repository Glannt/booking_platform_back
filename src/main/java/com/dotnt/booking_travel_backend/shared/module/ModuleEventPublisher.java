package com.dotnt.booking_travel_backend.shared.module;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ModuleEventPublisher {
    private final ApplicationEventPublisher publisher;

    public <T> T publishRequest(String requestType, Object payload, Class<T> responseClass) {
        CompletableFuture<T> future = new CompletableFuture<>();
        ModuleRequestEvent<T> ev = new ModuleRequestEvent<>(this, requestType, payload, future);
        publisher.publishEvent(ev);
        try {
            // wait up to 5 seconds
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Module request timed out or failed: " + requestType, e);
        }
    }
}

