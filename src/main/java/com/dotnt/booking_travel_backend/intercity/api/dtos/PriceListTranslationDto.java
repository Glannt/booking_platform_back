package com.dotnt.booking_travel_backend.intercity.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceListTranslationDto {
    private Long id;
    private Long priceListId;
    private String locale;
    private String name;
    private String description;
}
