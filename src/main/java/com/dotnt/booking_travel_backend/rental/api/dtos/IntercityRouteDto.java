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
    private BigDecimal originLat;
    private BigDecimal originLng;
    private BigDecimal destinationLat;
    private BigDecimal destinationLng;
    private String originName;
    private String destinationName;
    private Boolean active;
    private Boolean hasDepartureTimes;
    private Boolean supportsShared;
    private String sharedVehicleName;
    private BigDecimal sharedPriceOneWay;
    private BigDecimal sharedPriceRoundTrip;
    private Long thumbnailId;
    private String thumbnailUrl;
    private List<Long> imageIds;
    private List<IntercityRouteTranslationDto> translations;
    private List<IntercityRoutePriceDto> prices;
    private List<IntercityDepartureTimeDto> departureTimes;
}
