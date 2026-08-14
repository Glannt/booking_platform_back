package com.dotnt.booking_travel_backend.fleet.api.dtos;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleCategoryDTO {
    private Long id;
    private String code;
    private Integer minSeat;
    private Integer maxSeat;
    private Integer minLuggage;
    private Integer maxLuggage;
    private String unitLuggage;
    private Integer luggageWeight;
    private Boolean active;
    private Boolean isDefault;
    private Long thumbnailId;
    private String thumbnailUrl;
    private List<VehicleCategoryTranslationDTO> translations;
}
