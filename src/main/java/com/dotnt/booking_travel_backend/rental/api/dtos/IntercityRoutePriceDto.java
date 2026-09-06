package com.dotnt.booking_travel_backend.rental.api.dtos;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntercityRoutePriceDto {
    private Long id;
    private Long routeId;
    private Long vehicleCategoryId;
    private BigDecimal perKmAmount;
    private String pricingType;
    private BigDecimal fixedPriceOneWay;
    private BigDecimal fixedPriceRoundTrip;
    private BigDecimal roundTripPrice;
    private BigDecimal roundTripPerKmAmount;
    private String currency;
    private boolean active;
}
