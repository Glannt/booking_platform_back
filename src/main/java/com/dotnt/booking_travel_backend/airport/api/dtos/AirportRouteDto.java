package com.dotnt.booking_travel_backend.airport.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportRouteDto {
    private Long id;
    private String code;
    private String airportName;
    private String pickupLocation;
    private String dropoffLocation;
    private Double distance;
    private Integer duration;
    private boolean active;
}

