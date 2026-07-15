package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourOptionTranslationDto {
    private Long id;
    private String locale;
    private String content;
}
