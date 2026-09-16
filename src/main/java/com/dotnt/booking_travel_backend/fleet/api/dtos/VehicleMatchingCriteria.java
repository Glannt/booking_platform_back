package com.dotnt.booking_travel_backend.fleet.api.dtos;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleMatchingCriteria {
    private Long categoryId;
    private Long seatAddOnId;
    private Long fuelAddOnId;
    private List<Long> additionalAddOnIds;
    @Builder.Default
    private Boolean activeOnly = true;
    @Builder.Default
    private Boolean canRentOnly = true;
}
