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
    /**
     * The deposit of the same price row that produced {@link #totalPrice} — one row, one source.
     * Null means that row never had one, which the caller must be able to tell from a configured
     * rate, so nothing here is ever substituted with a default.
     */
    private String depositType;
    private BigDecimal depositRate;
    private BigDecimal depositAmount;
}
