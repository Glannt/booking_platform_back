package com.dotnt.booking_travel_backend.shared.deposit.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositCalculationRequest {
    private BigDecimal totalAmount;
    private String depositType; // PERCENT or FIXED
    private BigDecimal depositRate; // % rate if PERCENT
    private BigDecimal fixedDepositAmount; // fixed amount if FIXED
}
