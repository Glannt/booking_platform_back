package com.dotnt.booking_travel_backend.price.api.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceDto {
    private Long id;
    private String businessType;
    private Long businessId;
    private Long vehicleCategoryId;
    private String pricingUnit;
    private BigDecimal amount;
    private String currency;
    private Instant effectiveFrom;
    private Instant effectiveTo;
    private boolean active;
}

