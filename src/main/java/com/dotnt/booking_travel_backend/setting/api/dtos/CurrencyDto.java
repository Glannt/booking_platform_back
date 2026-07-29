package com.dotnt.booking_travel_backend.setting.api.dtos;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyDto {
    private Long id;
    private String code;
    private String symbol;
    private BigDecimal rateToVnd;
    private boolean active;
    private java.util.List<CurrencyTranslationDto> translations;
}
