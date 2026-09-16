package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourConfigDto {
    private Long id;
    private int recommendedLimit;
    private boolean autoRecommendEnabled;
}
