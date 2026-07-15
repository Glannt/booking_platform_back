package com.dotnt.booking_travel_backend.shared.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class SlidingWindowRateLimiter {

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        long now = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000L;

        return windows.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart > windowMs) {
                return new Window(now, 1);
            }
            if (existing.count < maxRequests) {
                existing.count++;
                return existing;
            }
            return existing; // deny
        }).count <= maxRequests;
    }

    private static class Window {
        long windowStart;
        int count;

        Window(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
