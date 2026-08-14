package com.dotnt.booking_travel_backend.surcharge.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurchargeLineItem {
    private Long ruleId;
    private String code;
    private String name;
    private BigDecimal amount;
    private String calculationType;
    private BigDecimal rate;
}
