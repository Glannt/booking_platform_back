package com.dotnt.booking_travel_backend.airport.api.dtos;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportTransferPriceDto {
    private Long id;
    private Long airportRouteId;
    private Long vehicleCategoryId;
    private BigDecimal baseKm;
    private BigDecimal baseAmount;
    private BigDecimal perKmAmount;
    @Builder.Default
    private BigDecimal zoneBufferKm = new BigDecimal("2.00");
    private boolean fixedOvercharge;
    private BigDecimal feeOverchargeFixed;
    private String currency;
    private boolean active;
    @Builder.Default
    private String depositType = "PERCENT";
    @Builder.Default
    private BigDecimal depositRate = new BigDecimal("20.00");
    private BigDecimal depositAmount;
}