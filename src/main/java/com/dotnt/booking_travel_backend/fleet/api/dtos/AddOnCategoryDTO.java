package com.dotnt.booking_travel_backend.fleet.api.dtos;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddOnCategoryDTO {
    private Long id;
    private String code;
    private String icon;
    private Integer displayOrder;
    private Boolean active;
    private Boolean isFilterable;
    private List<AddOnCategoryTranslationDTO> translations;
    private List<AddOnDTO> addOns;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
