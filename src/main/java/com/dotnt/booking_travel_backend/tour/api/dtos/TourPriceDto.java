package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourPriceDto {
    private Long id;
    private Long vehicleCategoryId;
    private String currency;
    private BigDecimal amount;
    private boolean active;
}
