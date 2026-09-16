package com.dotnt.booking_travel_backend.fleet.api.dtos;

import java.util.List;

public record CreateAmenityRequest(
        Long categoryId,
        String categoryCode,
        String code,
        String icon,
        Integer displayOrder,
        Boolean active,
        List<AmenityTranslationDTO> translations
) {}
