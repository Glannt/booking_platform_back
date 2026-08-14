package com.dotnt.booking_travel_backend.shared.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PriceMath {
    private PriceMath() {
    }

    /**
     * Rounds to the nearest 1000 VND (HALF_UP) and normalizes scale to 0 so JSON
     * serializes as plain integer (e.g. 350000), never scientific notation.
     */
    public static BigDecimal roundToThousand(BigDecimal value) {
        if (value == null) return BigDecimal.ZERO;
        return value.setScale(-3, RoundingMode.HALF_UP).setScale(0);
    }
}
