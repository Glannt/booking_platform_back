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
    private Double latitude;
    private Double longitude;
    private boolean active;
    private boolean isDefault;
    private List<AirportTranslationDto> translations;
}
