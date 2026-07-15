package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourActivityTranslationDto {
    private Long id;
    private Long tourActivityId;
    private String locale;
    private String name;
    private String description;
}

