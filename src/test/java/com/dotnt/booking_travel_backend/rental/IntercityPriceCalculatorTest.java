package com.dotnt.booking_travel_backend.rental;

import com.dotnt.booking_travel_backend.rental.internal.service.IntercityPriceCalculator;
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
}
