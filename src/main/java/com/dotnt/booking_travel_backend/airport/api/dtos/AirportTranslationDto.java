package com.dotnt.booking_travel_backend.airport.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportTranslationDto {
    private Long id;
    private String locale;
    private String name;
    private String description;
}
