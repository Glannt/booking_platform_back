package com.dotnt.booking_travel_backend.intercity;

import com.dotnt.booking_travel_backend.intercity.internal.service.IntercityPriceCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntercityPriceCalculatorTest {

    @Test
    void oneWayIsDistanceTimesPerKm() {
        BigDecimal price = IntercityPriceCalculator.price(new BigDecimal("135"), new BigDecimal("10000"), false);
        assertEquals(0, new BigDecimal("1350000").compareTo(price));
    }

    @Test
    void roundTripIsTwiceOneWay() {
        BigDecimal oneWay = IntercityPriceCalculator.price(new BigDecimal("135"), new BigDecimal("10000"), false);
        BigDecimal roundTrip = IntercityPriceCalculator.price(new BigDecimal("135"), new BigDecimal("10000"), true);
        assertEquals(0, oneWay.multiply(BigDecimal.valueOf(2)).compareTo(roundTrip));
    }

    @Test
    void roundsToNearestThousand() {
        BigDecimal price = IntercityPriceCalculator.price(new BigDecimal("9.5"), new BigDecimal("11000"), false);
        assertEquals(0, new BigDecimal("105000").compareTo(price));
    }

    @Test
    void nullDistanceIsZero() {
        BigDecimal price = IntercityPriceCalculator.price(null, new BigDecimal("10000"), false);
        assertEquals(0, BigDecimal.ZERO.compareTo(price));
    }

    @Test
    void nullPerKmReturnsZero() {
        BigDecimal price = IntercityPriceCalculator.price(new BigDecimal("135"), null, false);
        assertEquals(0, BigDecimal.ZERO.compareTo(price));
    }

    @Test
    void pricePrivateFixedWithRoundTripOverride() {
        BigDecimal fixedOneWay = new BigDecimal("1200000");
        BigDecimal fixedRoundTrip = new BigDecimal("2100000"); // Override, not 2.4m

        BigDecimal oneWay = IntercityPriceCalculator.pricePrivate(
                new BigDecimal("135"), "FIXED", null, fixedOneWay, fixedRoundTrip, null, null, false);
        assertEquals(0, fixedOneWay.compareTo(oneWay));

        BigDecimal roundTrip = IntercityPriceCalculator.pricePrivate(
                new BigDecimal("135"), "FIXED", null, fixedOneWay, fixedRoundTrip, null, null, true);
        assertEquals(0, fixedRoundTrip.compareTo(roundTrip));
    }

    @Test
    void pricePrivatePerKmWithRoundTripOverride() {
        BigDecimal dist = new BigDecimal("135");
        BigDecimal perKm = new BigDecimal("10000");
        BigDecimal roundTripOverride = new BigDecimal("2500000"); // Override, not 2.7m

        BigDecimal roundTrip = IntercityPriceCalculator.pricePrivate(
                dist, "PER_KM", perKm, null, null, roundTripOverride, null, true);
        assertEquals(0, roundTripOverride.compareTo(roundTrip));
    }

    @Test
    void pricePrivatePerKmWithRoundTripPerKmRate() {
        BigDecimal dist = new BigDecimal("100");
        BigDecimal perKm = new BigDecimal("10000"); // 1 way = 1m
        BigDecimal roundTripKmRate = new BigDecimal("8500"); // round trip = 100 * 8500 * 2 = 1.7m

        BigDecimal roundTrip = IntercityPriceCalculator.pricePrivate(
                dist, "PER_KM", perKm, null, null, null, roundTripKmRate, true);
        assertEquals(0, new BigDecimal("1700000").compareTo(roundTrip));
    }

    @Test
    void priceSharedPerPaxAndRoundTrip() {
        BigDecimal oneWayPax = new BigDecimal("250000");
        BigDecimal roundTripPax = new BigDecimal("480000"); // Discounted round trip

        // 1 pax one way
        BigDecimal p1 = IntercityPriceCalculator.priceShared(oneWayPax, roundTripPax, 1, false);
        assertEquals(0, new BigDecimal("250000").compareTo(p1));

        // 3 pax one way
        BigDecimal p3 = IntercityPriceCalculator.priceShared(oneWayPax, roundTripPax, 3, false);
        assertEquals(0, new BigDecimal("750000").compareTo(p3));

        // 2 pax round trip with override
        BigDecimal p2Round = IntercityPriceCalculator.priceShared(oneWayPax, roundTripPax, 2, true);
        assertEquals(0, new BigDecimal("960000").compareTo(p2Round));

        // 2 pax round trip without override (fallback 2 * 250k)
        BigDecimal p2RoundFallback = IntercityPriceCalculator.priceShared(oneWayPax, null, 2, true);
        assertEquals(0, new BigDecimal("1000000").compareTo(p2RoundFallback));
    }
}
