package com.dotnt.booking_travel_backend.fleet.internal.service;

import com.dotnt.booking_travel_backend.fleet.api.dtos.CreateVehicleCategoryRequest;
import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleCategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IVehicleCategory {
    CreateVehicleCategoryRequest createVehicleCategory(VehicleCategoryDTO vehicleCategoryDTO);
    Page<VehicleCategoryDTO> getVehicleCategories(Pageable pageable, String search);
}
