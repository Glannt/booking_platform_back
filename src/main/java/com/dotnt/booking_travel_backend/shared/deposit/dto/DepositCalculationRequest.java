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
    private String depositType; // PERCENT, FIXED, PER_PERSON
    private BigDecimal depositRate; // % rate if PERCENT
    private BigDecimal fixedDepositAmount; // fixed amount if FIXED
    private Integer adultCount; // for tour PER_PERSON
    private Integer childCount; // for tour PER_PERSON
    private BigDecimal depositPerAdult; // for tour PER_PERSON
    private BigDecimal depositPerChild; // for tour PER_PERSON
    private String serviceType; // TOUR, SELF_DRIVE, INTERCITY, AIRPORT
}
