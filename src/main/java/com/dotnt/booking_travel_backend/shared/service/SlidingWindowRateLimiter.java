package com.dotnt.booking_travel_backend.shared.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SlidingWindowRateLimiter {

    private final Cache<String, Window> windows = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(50000)
            .build();

    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        long now = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000L;

        Window result = windows.asMap().compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart > windowMs) {
                return new Window(now, 1);
            }
            if (existing.count < maxRequests) {
                existing.count++;
                return existing;
            }
            return existing; // deny
        });

        return result != null && result.count <= maxRequests;
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
