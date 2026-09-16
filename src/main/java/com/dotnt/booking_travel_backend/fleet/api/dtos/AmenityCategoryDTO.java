package com.dotnt.booking_travel_backend.fleet.api.dtos;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmenityCategoryDTO {
    private Long id;
    private String code;
    private String icon;
    private Integer displayOrder;
    private Boolean active;
    private List<AmenityCategoryTranslationDTO> translations;
    private List<AmenityDTO> amenities;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
