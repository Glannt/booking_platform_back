package com.dotnt.booking_travel_backend.location.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDto {
    private Long id;
    private Double latitude;
    private Double longitude;
    private String category;
    private Boolean active;
    private String name;
    private String address;
    private String description;
    private List<LocationTranslationDto> translations;
}
