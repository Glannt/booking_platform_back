package com.dotnt.booking_travel_backend.shared.deposit.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositCalculationResult {
    private String depositType; // PERCENT, FIXED, PER_PERSON
    private BigDecimal depositRate; // effective percentage
    private BigDecimal depositAmount; // calculated deposit amount in currency
    private BigDecimal remainingAmount; // totalAmount - depositAmount
    private String description; // human-readable explanation from data
}
