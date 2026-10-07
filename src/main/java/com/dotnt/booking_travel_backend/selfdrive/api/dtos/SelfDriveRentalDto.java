package com.dotnt.booking_travel_backend.selfdrive.api.dtos;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfDriveRentalDto {
    private Long id;
    private Long rentalTypeId;
    private SelfDriveTypeDto rentalType;
    private Long vehicleCategoryId;
    private boolean driverRequired;
    private BigDecimal deposit;
    private String depositType; // FIXED, PERCENT
    private BigDecimal depositRate;
    private Integer minimumDays;
    private boolean active;

    @com.fasterxml.jackson.annotation.JsonProperty("isContactOnly")
    @com.fasterxml.jackson.annotation.JsonAlias({"contactOnly", "isContactOnly"})
    @Builder.Default
    private Boolean isContactOnly = false;

    @Builder.Default
    private Integer sortOrder = 0;

    public boolean isContactOnly() {
        return Boolean.TRUE.equals(this.isContactOnly);
    }
}
