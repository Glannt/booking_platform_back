package com.dotnt.booking_travel_backend.setting.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyTranslationDto {
    private Long id;
    private String locale;
    private String name;
}
