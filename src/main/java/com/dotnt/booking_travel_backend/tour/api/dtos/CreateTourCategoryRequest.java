package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTourCategoryRequest {
    private String code;
    private String icon;
    private Integer sortOrder;
    private Boolean isActive;
    private String title;
    private String description;
    private List<TourCategoryTranslationDTO> translations;
}
