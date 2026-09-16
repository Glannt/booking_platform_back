package com.dotnt.booking_travel_backend.promotion.internal.scope;

import com.dotnt.booking_travel_backend.shared.enums.ScopeType;
import org.springframework.stereotype.Component;

@Component
public class GlobalScopeResolver implements ScopeResolver {

    @Override
    public ScopeType supports() {
        return ScopeType.GLOBAL;
    }

    @Override
    public boolean matches(CartItem cartItem, String scopeRefId) {
        return true;
    }
}
