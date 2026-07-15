package com.dotnt.booking_travel_backend.fleet.api.dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateVehicleCategory(
        @NotBlank String code,
        @NotBlank int seat,
        @NotBlank int luggage,
        boolean active
) {
}
