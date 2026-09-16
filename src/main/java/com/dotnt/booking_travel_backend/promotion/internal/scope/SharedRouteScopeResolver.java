package com.dotnt.booking_travel_backend.promotion.internal.scope;

import com.dotnt.booking_travel_backend.shared.enums.ScopeType;
import org.springframework.stereotype.Component;

@Component
public class SharedRouteScopeResolver implements ScopeResolver {

    @Override
    public ScopeType supports() {
        return ScopeType.SHARED_ROUTE;
    }

    @Override
    public boolean matches(CartItem item, String scopeRefId) {
        if (item == null) return true;
        if (item.getModule() == null || !"SHARED_ROUTE".equalsIgnoreCase(item.getModule())) {
            return false;
        }
        return scopeRefId == null || scopeRefId.isBlank() || scopeRefId.equalsIgnoreCase(item.getProductId());
    }
}
