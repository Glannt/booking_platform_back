package com.dotnt.booking_travel_backend.promotion.internal.scope;

import com.dotnt.booking_travel_backend.shared.enums.ScopeType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScopeResolverRegistry {

    private final List<ScopeResolver> resolvers;
    private Map<ScopeType, ScopeResolver> byType;

    @PostConstruct
    void init() {
        byType = resolvers.stream().collect(Collectors.toMap(ScopeResolver::supports, r -> r));
    }

    public boolean matches(ScopeType type, CartItem item, String scopeRefId) {
        if (type == ScopeType.GLOBAL) return true;
        if (item == null) return true;
        ScopeResolver r = byType.get(type);
        if (r == null) {
            throw new IllegalStateException("Chưa implement ScopeResolver cho: " + type);
        }
        return r.matches(item, scopeRefId);
    }
}
