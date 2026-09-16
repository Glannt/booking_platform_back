package com.dotnt.booking_travel_backend.fleet.api.dtos;

import java.util.List;

public record CreateAddOnCategoryRequest(
        String code,
        String icon,
        Integer displayOrder,
        Boolean active,
        Boolean isFilterable,
        List<AddOnCategoryTranslationDTO> translations
) {}
