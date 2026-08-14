package com.dotnt.booking_travel_backend.surcharge.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurchargeResult {
    @Builder.Default
    private List<SurchargeLineItem> lineItems = new ArrayList<>();
    private BigDecimal totalSurcharge;
    private BigDecimal finalAmount;

    public static SurchargeResult empty(BigDecimal basePrice) {
        BigDecimal price = basePrice != null ? basePrice : BigDecimal.ZERO;
        return SurchargeResult.builder()
                .lineItems(new ArrayList<>())
                .totalSurcharge(BigDecimal.ZERO)
                .finalAmount(price)
                .build();
    }
}
