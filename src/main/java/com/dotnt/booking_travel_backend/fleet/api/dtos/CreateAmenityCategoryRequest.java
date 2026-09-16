package com.dotnt.booking_travel_backend.fleet.api.dtos;

import java.util.List;

public record CreateAmenityCategoryRequest(
        String code,
        String icon,
        Integer displayOrder,
        Boolean active,
        List<AmenityCategoryTranslationDTO> translations
) {}
