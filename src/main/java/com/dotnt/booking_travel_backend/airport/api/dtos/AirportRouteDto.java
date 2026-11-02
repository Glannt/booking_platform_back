package com.dotnt.booking_travel_backend.airport.api.dtos;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportRouteDto {
    private Long id;
    private String code;
    private Long airportId;
    private String airportName;
    private String airportCode;
    private String name;
    private BigDecimal distanceKm;
    private String originName;
    private String destinationName;
    private BigDecimal originLat;
    private BigDecimal originLng;
    private BigDecimal destinationLat;
    private BigDecimal destinationLng;
    private boolean active;
    @Builder.Default
    private String depositType = "PERCENT";
    @Builder.Default
    private BigDecimal depositRate = new BigDecimal("20.00");
    private BigDecimal depositAmount;
}

