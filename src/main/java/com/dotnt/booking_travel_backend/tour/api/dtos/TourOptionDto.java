package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourOptionDto {
    private Long id;
    private String iconUrl;
    private String colorText;
    private List<TourOptionTranslationDto> translations;
}
