package com.dotnt.booking_travel_backend.fleet.api.dtos;

public record CreateVehicleRequest(
        String brand,
        String model,
        Integer year,
        String fuelType,
        Boolean active,
        Long vehicleCategoryId,
        Integer luggage,
        String luggageUnit,
        Long thumbnailId,
        java.util.List<Long> imageIds
) {
}
