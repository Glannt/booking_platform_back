package com.dotnt.booking_travel_backend.fleet.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateVehicleCategoryRequest(
        @NotBlank String code,
        Integer minSeat,
        Integer maxSeat,
        Integer minLuggage,
        Integer maxLuggage,
        String unitLuggage,
        Boolean active,
        Boolean isDefault,
        Long thumbnailId,
        Integer totalVehicles,
        Integer availableVehicles,
        @NotEmpty List<VehicleCategoryTranslationDTO> translations
) {
}
