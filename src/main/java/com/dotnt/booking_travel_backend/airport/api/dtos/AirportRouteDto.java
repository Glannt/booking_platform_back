package com.dotnt.booking_travel_backend.airport.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportRouteDto {
    private Long id;
    private String code;
    private Long airportId;
    private String name;
    private boolean active;
}

