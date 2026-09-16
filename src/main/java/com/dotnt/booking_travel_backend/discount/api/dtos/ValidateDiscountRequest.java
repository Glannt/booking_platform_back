package com.dotnt.booking_travel_backend.discount.api.dtos;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateDiscountRequest {
    private String code;
    private String scopeType; // INTERCITY_ROUTE, SHARED_ROUTE, SELF_DRIVE, TOUR, AIRPORT_TRANSFER
    private String scopeRefId;
    private BigDecimal orderAmount;
    private Integer days;
    private String customerSegment;
}
