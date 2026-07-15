package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourActivityDto {
    private Long id;
    private Long tourDayId;
    private Instant startTime;
    private Instant endTime;
    private String location;
    private List<TourActivityTranslationDto> translations;
}

