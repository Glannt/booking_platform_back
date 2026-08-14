package com.dotnt.booking_travel_backend.rental.internal.service;

import com.dotnt.booking_travel_backend.shared.utils.PriceMath;

import java.math.BigDecimal;

/**
 * per-km intercity pricing.
 * oneWay = distanceKm * perKmAmount
 * roundTrip = 2 * oneWay
 */
public final class IntercityPriceCalculator {

    private IntercityPriceCalculator() {
    }

    public static BigDecimal price(BigDecimal distanceKm, BigDecimal perKmAmount, boolean roundTrip) {
        if (perKmAmount == null) return BigDecimal.ZERO;
        BigDecimal dist = distanceKm != null ? distanceKm : BigDecimal.ZERO;
        BigDecimal oneWay = PriceMath.roundToThousand(dist.multiply(perKmAmount));
        return roundTrip ? oneWay.multiply(BigDecimal.valueOf(2)) : oneWay;
    }
}
