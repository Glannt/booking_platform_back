package com.dotnt.booking_travel_backend.rental.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntercityRouteTranslationDto {
    private Long id;
    private Long routeId;
    private String locale;
    private String name;
    private String description;
}
