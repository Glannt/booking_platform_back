package com.dotnt.booking_travel_backend.setting.api.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCurrencyRequest {
    private String code;
    private String symbol;
    private BigDecimal rateToVnd;
    private boolean active;
    private List<CurrencyTranslationDto> translations;
}
