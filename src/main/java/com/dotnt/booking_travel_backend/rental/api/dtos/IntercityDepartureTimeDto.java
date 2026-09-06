package com.dotnt.booking_travel_backend.rental.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntercityDepartureTimeDto {
    private Long id;
    private Long routeId;
    private String departureTime;
    private String direction;
    private int orderIndex;
    private boolean active;
}
