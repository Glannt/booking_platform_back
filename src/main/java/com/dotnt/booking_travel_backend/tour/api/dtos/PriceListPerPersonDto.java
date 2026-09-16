package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceListPerPersonDto {
    private Long id;
    private String passengerType; // 'ADULT' | 'CHILD' | 'INFANT'
    private BigDecimal pricePerPerson;
    private Integer minPax;
    private Integer maxPax;
    private Boolean active;
    private Boolean applyAgeCondition;
    private Integer minAge;
    private Integer maxAge;
    private Boolean applyHeightCondition;
    private Integer minHeight;
    private Integer maxHeight;
}
