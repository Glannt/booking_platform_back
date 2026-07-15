package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourDayDto {
    private Long id;
    private Long tourId;
    private int dayNumber;
}

