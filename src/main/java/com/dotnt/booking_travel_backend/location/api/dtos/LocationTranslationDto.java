package com.dotnt.booking_travel_backend.location.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationTranslationDto {
    private Long id;
    private String locale;
    private String name;
    private String address;
    private String description;
}
