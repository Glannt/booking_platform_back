package com.dotnt.booking_travel_backend.intercity.api.dtos;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntercityPriceCalculateRequest {
    private Long routeId;
    private Long vehicleCategoryId;
    private String priceTypeCode; // ONE_WAY, ROUND_TRIP_SAME_DAY, DAILY_RATE
    private LocalDate departureDate;
    private LocalDate returnDate;
    private Integer passengerCount;
    private String locale;
}
