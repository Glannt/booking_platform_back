package com.dotnt.booking_travel_backend.promotion.internal.scope;

import com.dotnt.booking_travel_backend.shared.enums.ScopeType;

public interface ScopeResolver {

    ScopeType supports();

    /**
     * Trả về true nếu cartItem này thuộc phạm vi của scope record đang xét.
     */
    boolean matches(CartItem cartItem, String scopeRefId);
}
