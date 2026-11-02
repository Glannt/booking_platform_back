package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourDayDto {
    private Long id;
    private Long tourId;

    @Builder.Default
    private Integer dayNumber = 1;

    private List<TourDayTranslationDto> translations;
    private List<TourActivityDto> activities;
}

