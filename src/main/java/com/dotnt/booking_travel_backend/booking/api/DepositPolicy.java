package com.dotnt.booking_travel_backend.booking.api;

import com.dotnt.booking_travel_backend.shared.deposit.DepositEngine;
import com.dotnt.booking_travel_backend.shared.deposit.dto.DepositCalculationRequest;
import com.dotnt.booking_travel_backend.shared.deposit.dto.DepositCalculationResult;

import java.math.BigDecimal;

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
