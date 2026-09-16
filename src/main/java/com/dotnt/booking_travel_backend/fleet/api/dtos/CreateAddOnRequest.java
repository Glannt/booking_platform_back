package com.dotnt.booking_travel_backend.fleet.api.dtos;

import java.util.List;

public record CreateAddOnRequest(
        Long categoryId,
        String categoryCode,
        String code,
        String value,
        String icon,
        Integer displayOrder,
        Boolean active,
        List<AddOnTranslationDTO> translations
) {}
