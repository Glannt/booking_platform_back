package com.dotnt.booking_travel_backend.rental.api.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntercitySeasonalSurchargeDto {
    private Long id;
    private Long routeId;
    private String seasonName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal surchargePercent;
    private boolean active;
}
