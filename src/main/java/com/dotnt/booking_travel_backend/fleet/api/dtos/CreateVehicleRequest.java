package com.dotnt.booking_travel_backend.fleet.api.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public record CreateVehicleRequest(
        String brand,
        String model,
        String licensePlate,
        Integer year,
        String fuelType,
        Boolean active,
        Boolean canRent,
        @JsonAlias({"typeId", "vehicleCategoryId"})
        Long vehicleCategoryId,
        Integer luggage,
        String luggageUnit,
        Long thumbnailId,
        List<Long> imageIds,
        List<Long> addOnIds,
        List<Long> amenityIds
) {
}
