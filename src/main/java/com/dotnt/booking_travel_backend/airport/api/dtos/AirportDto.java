package com.dotnt.booking_travel_backend.airport.api.dtos;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportDto {
    private Long id;
    private String code;
    private String name;
    private String location;
    private boolean active;
    private List<AirportTranslationDto> translations;
}
