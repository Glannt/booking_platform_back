package com.dotnt.booking_travel_backend.airport.internal.service;

import com.dotnt.booking_travel_backend.airport.internal.entity.AirportTransferPrice;
import com.dotnt.booking_travel_backend.shared.utils.PriceMath;

import java.math.BigDecimal;

/**
 * km-based airport transfer pricing.
 * oneWay = baseAmount + max(0, actualKm - baseKm) * perKmAmount
 * roundTrip = 2 * oneWay
 */
public final class AirportTransferPriceCalculator {

    private AirportTransferPriceCalculator() {
    }

    public static BigDecimal oneWay(AirportTransferPrice price, BigDecimal actualKm) {
        if (price == null || price.getBaseAmount() == null) return BigDecimal.ZERO;
        BigDecimal km = actualKm != null ? actualKm : price.getBaseKm();
        BigDecimal overage = km.subtract(price.getBaseKm()).max(BigDecimal.ZERO);
        BigDecimal raw = price.getBaseAmount();
        
        if (overage.compareTo(BigDecimal.ZERO) > 0) {
            if (price.isFixedOvercharge()) {
                BigDecimal fixedFee = price.getFeeOverchargeFixed() != null 
                        ? price.getFeeOverchargeFixed() 
                        : new BigDecimal("100000"); // mặc định 100k
                raw = raw.add(fixedFee);
            } else {
                raw = raw.add(overage.multiply(price.getPerKmAmount()));
            }
        }
        
        return PriceMath.roundToThousand(raw);
    }

    public static BigDecimal roundTrip(AirportTransferPrice price, BigDecimal actualKm) {
        return oneWay(price, actualKm).multiply(BigDecimal.valueOf(2));
    }
}
