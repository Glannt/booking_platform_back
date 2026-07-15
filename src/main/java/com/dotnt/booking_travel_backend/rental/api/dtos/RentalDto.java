package com.dotnt.booking_travel_backend.rental.api.dtos;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalDto {
    private Long id;
    private Long rentalTypeId;
    private com.dotnt.booking_travel_backend.rental.api.dtos.RentalTypeDto rentalType;
    private Long vehicleCategoryId;
    private boolean driverRequired;
    private BigDecimal deposit;
    private Integer minimumDays;
    private boolean active;
}

