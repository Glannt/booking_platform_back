package com.dotnt.booking_travel_backend.intercity.api.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceListDto {
    private Long id;
    private Long priceTypeId;
    private String priceTypeCode; // ONE_WAY, ROUND_TRIP_SAME_DAY, DAILY_RATE
    private String priceTypeCategory; // IN_DAY, DAILY_RATE
    private String scopeType; // INTERCITY_ROUTE
    private String scopeRefId; // routeId
    private Long routeId;
    private Long vehicleCategoryId;
    private String scopeMeta;
    private String currency;
    private BigDecimal basePrice;
    /**
     * Deposit for this price row. All three are null when the row has no deposit configured —
     * callers must treat that as "no deposit" and hide the UI, not substitute a default rate.
     */
    private String depositType; // PERCENT, FIXED, PER_PERSON
    private BigDecimal depositRate;
    private BigDecimal depositAmount;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String status; // ACTIVE, INACTIVE
    private Boolean active;
    private Boolean inDayActive;
    private Boolean dailyRateActive;

    private List<PriceListTranslationDto> translations;

    // Route details (ONE_WAY, ROUND_TRIP_SAME_DAY)
    private BigDecimal waitingFee;
    private Integer maxWaitingMin;

    // Daily rate details (DAILY_RATE)
    private Integer minDays;
    private Integer maxDays;
    private BigDecimal kmIncludedPerDay;
    private BigDecimal overagePricePerKm;

    // Helper fields for batch/composite operations from Admin UI
    private BigDecimal oneWayPrice;
    private BigDecimal roundTripPrice;
    private BigDecimal dailyRatePrice;
}
