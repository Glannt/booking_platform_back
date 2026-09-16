package com.dotnt.booking_travel_backend.intercity.api.dtos;

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
    private String slug;
    private String components;
    private Long thumbnailId;
    private String thumbnailUrl;
    private List<Long> imageIds;
    private List<IntercityRouteTranslationDto> translations;
    private List<IntercityRoutePriceDto> prices;
    private List<IntercityDepartureTimeDto> departureTimes;
    private Long linkedSharedRouteId;
    private Boolean hasSharedRoute;

    @com.fasterxml.jackson.annotation.JsonProperty("isContactOnly")
    @com.fasterxml.jackson.annotation.JsonAlias({"contactOnly", "isContactOnly"})
    @Builder.Default
    private Boolean isContactOnly = false;

    public boolean isContactOnly() {
        return Boolean.TRUE.equals(this.isContactOnly);
    }
}
