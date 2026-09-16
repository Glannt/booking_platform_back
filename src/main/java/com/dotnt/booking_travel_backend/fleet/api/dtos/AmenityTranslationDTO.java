package com.dotnt.booking_travel_backend.fleet.api.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmenityTranslationDTO {
    private Long id;
    private String locale;
    private String name;
    private String description;
}
