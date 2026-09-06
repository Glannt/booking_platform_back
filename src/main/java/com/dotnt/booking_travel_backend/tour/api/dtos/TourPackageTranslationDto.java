package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourPackageTranslationDto {
    private Long id;
    private String locale;
    private String name;
    private String highlights;
    private String inclusions;
    private String exclusions;
    private String childPolicyNote;
    private String infantPolicyNote;
}
