package com.dotnt.booking_travel_backend.rental.api.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntercityRouteDto {
    private Long id;
    private String originCity;
    private String destinationCity;
    private BigDecimal distanceKm;
    private BigDecimal estimatedHours;
    private boolean active;
    private boolean hasDepartureTimes;
    private List<IntercityRouteTranslationDto> translations;
    private List<IntercityRoutePriceDto> prices;
    private List<IntercityDepartureTimeDto> departureTimes;
}
