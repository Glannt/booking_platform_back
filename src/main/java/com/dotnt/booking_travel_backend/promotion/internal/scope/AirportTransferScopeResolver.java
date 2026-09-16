package com.dotnt.booking_travel_backend.promotion.internal.scope;

import com.dotnt.booking_travel_backend.shared.enums.ScopeType;
import org.springframework.stereotype.Component;

@Component
public class AirportTransferScopeResolver implements ScopeResolver {

    @Override
    public ScopeType supports() {
        return ScopeType.AIRPORT_TRANSFER;
    }

    @Override
    public boolean matches(CartItem item, String scopeRefId) {
        if (item == null) return true;
        if (item.getModule() == null || (!"AIRPORT".equalsIgnoreCase(item.getModule()) && !"AIRPORT_TRANSFER".equalsIgnoreCase(item.getModule()))) {
            return false;
        }
        return scopeRefId == null || scopeRefId.isBlank() || scopeRefId.equalsIgnoreCase(item.getProductId());
    }
}
