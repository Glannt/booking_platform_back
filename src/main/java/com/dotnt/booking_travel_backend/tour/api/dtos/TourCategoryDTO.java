package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourCategoryDTO {
    private Long id;
    private String code;
    private String icon;
    private Integer sortOrder;
    private boolean active;
    private boolean isActive;
    private String title;
    private String description;
    @Builder.Default
    private List<TourCategoryTranslationDTO> translations = new ArrayList<>();
}
