package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTourCategoryRequest {
    private String title;
    private String description;
    private boolean isActive;
}
