package com.dotnt.booking_travel_backend.airport.internal.service;

import com.dotnt.booking_travel_backend.airport.internal.entity.AirportTransferPrice;
import com.dotnt.booking_travel_backend.shared.utils.PriceMath;

import java.math.BigDecimal;

/**
 * km-based airport transfer pricing with Zone Buffer support.
 * effectiveBaseKm = baseKm + zoneBufferKm (tolerance zone where base price is preserved)
 * overage = max(0, actualKm - effectiveBaseKm)
 * oneWay = baseAmount + overage * perKmAmount (or fixed overcharge)
 * roundTrip = 2 * oneWay
 */
public final class AirportTransferPriceCalculator {

    private AirportTransferPriceCalculator() {
    }

    public static BigDecimal oneWay(AirportTransferPrice price, BigDecimal actualKm) {
        if (price == null || price.getBaseAmount() == null) return BigDecimal.ZERO;
        BigDecimal baseKm = price.getBaseKm() != null ? price.getBaseKm() : BigDecimal.ZERO;
        BigDecimal zoneBuffer = price.getZoneBufferKm() != null ? price.getZoneBufferKm() : BigDecimal.ZERO;
        BigDecimal effectiveBaseKm = baseKm.add(zoneBuffer);

        BigDecimal km = actualKm != null ? actualKm : baseKm;
        BigDecimal overage = km.subtract(effectiveBaseKm).max(BigDecimal.ZERO);
        BigDecimal raw = price.getBaseAmount();
        
        if (overage.compareTo(BigDecimal.ZERO) > 0) {
            if (price.isFixedOvercharge()) {
                BigDecimal fixedFee = price.getFeeOverchargeFixed() != null 
                        ? price.getFeeOverchargeFixed() 
                        : new BigDecimal("100000"); // mặc định 100k
                raw = raw.add(fixedFee);
            } else {
                BigDecimal perKm = price.getPerKmAmount() != null ? price.getPerKmAmount() : BigDecimal.ZERO;
                raw = raw.add(overage.multiply(perKm));
            }
        }
        
        return PriceMath.roundToThousand(raw);
    }

    public static BigDecimal roundTrip(AirportTransferPrice price, BigDecimal actualKm) {
        return oneWay(price, actualKm).multiply(BigDecimal.valueOf(2));
    }
}
