package com.dotnt.booking_travel_backend.booking.api;

import com.dotnt.booking_travel_backend.shared.deposit.DepositEngine;
import com.dotnt.booking_travel_backend.shared.deposit.dto.DepositCalculationRequest;
import com.dotnt.booking_travel_backend.shared.deposit.dto.DepositCalculationResult;

import java.math.BigDecimal;

/**
 * @deprecated Legacy static facade. New code should use the shared deposit domain:
 * {@link com.dotnt.booking_travel_backend.shared.deposit.DepositPolicy} (persisted value
 * object / @Embeddable), {@link com.dotnt.booking_travel_backend.shared.deposit.DepositPolicies}
 * (mapping/normalization) and {@link com.dotnt.booking_travel_backend.shared.deposit.DepositEngine}
 * (calculation). Kept only as a compatibility shim for existing callers.
 *
 * This class is deprecated and will be removed in a future release. Please migrate to the new deposit domain.
 */
@Deprecated
public class DepositPolicy {
    public static final String TYPE_FIXED = DepositEngine.TYPE_FIXED;
    public static final String TYPE_RATE = "RATE";
    public static final String TYPE_PERCENT = DepositEngine.TYPE_PERCENT;
    public static final String TYPE_PER_PERSON = DepositEngine.TYPE_PER_PERSON;

    public static final BigDecimal DEFAULT_TOUR_RATE = DepositEngine.DEFAULT_TOUR_RATE;
    public static final BigDecimal DEFAULT_INTERCITY_RATE = DepositEngine.DEFAULT_INTERCITY_RATE;
    public static final BigDecimal DEFAULT_AIRPORT_RATE = DepositEngine.DEFAULT_AIRPORT_RATE;
    public static final BigDecimal DEFAULT_SELF_DRIVE_RATE = DepositEngine.DEFAULT_SELF_DRIVE_RATE;

    public static BigDecimal getDefaultRate(String serviceType) {
        return DepositEngine.getDefaultRate(serviceType);
    }

    public static BigDecimal calculateDeposit(
            String depositType,
            BigDecimal depositRate,
            BigDecimal fixedAmount,
            BigDecimal totalAmount,
            String serviceType
    ) {
        DepositCalculationResult res = DepositEngine.calculateDeposit(
                DepositCalculationRequest.builder()
                        .depositType(depositType)
                        .depositRate(depositRate)
                        .fixedDepositAmount(fixedAmount)
                        .totalAmount(totalAmount)
                        .serviceType(serviceType)
                        .build()
        );
        return res.getDepositAmount();
    }
}
