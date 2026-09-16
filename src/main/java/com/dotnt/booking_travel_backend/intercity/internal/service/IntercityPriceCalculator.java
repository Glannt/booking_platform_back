package com.dotnt.booking_travel_backend.intercity.internal.service;

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

    public static BigDecimal pricePrivate(
            BigDecimal distanceKm,
            String pricingType,
            BigDecimal perKmAmount,
            BigDecimal fixedPriceOneWay,
            BigDecimal fixedPriceRoundTrip,
            BigDecimal roundTripPrice,
            BigDecimal roundTripPerKmAmount,
            boolean roundTrip
    ) {
        if ("FIXED".equalsIgnoreCase(pricingType)) {
            BigDecimal oneWay = fixedPriceOneWay != null ? fixedPriceOneWay : BigDecimal.ZERO;
            if (roundTrip) {
                if (fixedPriceRoundTrip != null && fixedPriceRoundTrip.compareTo(BigDecimal.ZERO) > 0) {
                    return fixedPriceRoundTrip;
                }
                if (roundTripPrice != null && roundTripPrice.compareTo(BigDecimal.ZERO) > 0) {
                    return roundTripPrice;
                }
                return oneWay.multiply(BigDecimal.valueOf(2));
            }
            return oneWay;
        }

        // Default: PER_KM
        BigDecimal dist = distanceKm != null ? distanceKm : BigDecimal.ZERO;
        BigDecimal rate = perKmAmount != null ? perKmAmount : BigDecimal.ZERO;
        BigDecimal oneWay = PriceMath.roundToThousand(dist.multiply(rate));

        if (roundTrip) {
            if (roundTripPrice != null && roundTripPrice.compareTo(BigDecimal.ZERO) > 0) {
                return roundTripPrice;
            }
            if (roundTripPerKmAmount != null && roundTripPerKmAmount.compareTo(BigDecimal.ZERO) > 0) {
                return PriceMath.roundToThousand(dist.multiply(roundTripPerKmAmount).multiply(BigDecimal.valueOf(2)));
            }
            return oneWay.multiply(BigDecimal.valueOf(2));
        }
        return oneWay;
    }

    public static BigDecimal priceShared(
            BigDecimal sharedPriceOneWay,
            BigDecimal sharedPriceRoundTrip,
            int passengerCount,
            boolean roundTrip
    ) {
        int pax = Math.max(1, passengerCount);
        BigDecimal oneWay = sharedPriceOneWay != null ? sharedPriceOneWay : BigDecimal.ZERO;
        if (roundTrip) {
            BigDecimal round = (sharedPriceRoundTrip != null && sharedPriceRoundTrip.compareTo(BigDecimal.ZERO) > 0)
                    ? sharedPriceRoundTrip
                    : oneWay.multiply(BigDecimal.valueOf(2));
            return round.multiply(BigDecimal.valueOf(pax));
        }
        return oneWay.multiply(BigDecimal.valueOf(pax));
    }
}
