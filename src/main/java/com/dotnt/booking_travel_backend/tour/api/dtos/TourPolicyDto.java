package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourPolicyDto {
    private Long id;
    private String code;
    private String icon;
    private int sortOrder;
    private boolean active;
    private List<TourPolicyTranslationDto> translations;
}
