package com.dotnt.booking_travel_backend.fleet.api.dtos;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicAddOnCategoryDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private Integer displayOrder;
    private Boolean isFilterable;
    private List<PublicAddOnDTO> addOns;
}
