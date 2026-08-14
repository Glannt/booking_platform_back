package com.dotnt.booking_travel_backend.airport;

import com.dotnt.booking_travel_backend.airport.internal.entity.AirportTransferPrice;
import com.dotnt.booking_travel_backend.airport.internal.service.AirportTransferPriceCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AirportTransferPriceCalculatorTest {

    private AirportTransferPrice price(String baseKm, String baseAmount, String perKmAmount) {
        return AirportTransferPrice.builder()
                .baseKm(new BigDecimal(baseKm))
                .baseAmount(new BigDecimal(baseAmount))
                .perKmAmount(new BigDecimal(perKmAmount))
                .currency("VND")
                .active(true)
                .build();
    }

    @Test
    void oneWayWithinBaseKmReturnsBaseAmount() {
        BigDecimal price = AirportTransferPriceCalculator.oneWay(price("35", "300000", "10000"), new BigDecimal("30"));
        assertEquals(0, new BigDecimal("300000").compareTo(price));
    }

    @Test
    void oneWayAtExactBaseKmReturnsBaseAmount() {
        BigDecimal price = AirportTransferPriceCalculator.oneWay(price("35", "300000", "10000"), new BigDecimal("35"));
        assertEquals(0, new BigDecimal("300000").compareTo(price));
    }

    @Test
    void oneWayAboveBaseKmAddsPerKmOverage() {
        BigDecimal price = AirportTransferPriceCalculator.oneWay(price("35", "300000", "10000"), new BigDecimal("40"));
        assertEquals(0, new BigDecimal("350000").compareTo(price));
    }

    @Test
    void roundTripIsTwiceOneWay() {
        BigDecimal oneWay = AirportTransferPriceCalculator.oneWay(price("35", "300000", "10000"), new BigDecimal("40"));
        BigDecimal roundTrip = AirportTransferPriceCalculator.roundTrip(price("35", "300000", "10000"), new BigDecimal("40"));
        assertEquals(0, new BigDecimal("700000").compareTo(roundTrip));
        assertEquals(0, oneWay.multiply(BigDecimal.valueOf(2)).compareTo(roundTrip));
    }

    @Test
    void roundsToNearestThousand() {
        BigDecimal price = AirportTransferPriceCalculator.oneWay(price("0", "1000", "1000"), new BigDecimal("9.5"));
        assertEquals(0, new BigDecimal("11000").compareTo(price)); // 1000 + 9500 = 10500 -> HALF_UP -> 11000
    }

    @Test
    void nullActualKmFallsBackToBaseKm() {
        BigDecimal price = AirportTransferPriceCalculator.oneWay(price("35", "300000", "10000"), null);
        assertEquals(0, new BigDecimal("300000").compareTo(price));
    }

    @Test
    void nullPriceReturnsZero() {
        assertEquals(0, BigDecimal.ZERO.compareTo(AirportTransferPriceCalculator.oneWay(null, new BigDecimal("40"))));
    }
}
