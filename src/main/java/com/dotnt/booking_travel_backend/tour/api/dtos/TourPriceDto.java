package com.dotnt.booking_travel_backend.tour.api.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Builder.Default
    private Boolean active = true;

    @JsonIgnore
    public boolean isActive() {
        return active == null || active;
    }
}
