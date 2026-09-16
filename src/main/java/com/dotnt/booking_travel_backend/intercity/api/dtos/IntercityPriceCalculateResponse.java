package com.dotnt.booking_travel_backend.intercity.api.dtos;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntercityPriceCalculateResponse {
    private Long routeId;
    private Long vehicleCategoryId;
    private String priceTypeCode;
    private String priceTypeName;
    private BigDecimal basePrice;
    private Integer days;
    private BigDecimal totalPrice;
    private String currency;
    private boolean fallbackRoundTrip;
}
