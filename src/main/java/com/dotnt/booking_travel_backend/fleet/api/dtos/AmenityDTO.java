package com.dotnt.booking_travel_backend.fleet.api.dtos;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmenityDTO {
    private Long id;
    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private List<AmenityCategoryTranslationDTO> categoryTranslations;
    private String code;
    private String icon;
    private Integer displayOrder;
    private Boolean active;
    private List<AmenityTranslationDTO> translations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
