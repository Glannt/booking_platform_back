package com.dotnt.booking_travel_backend.fleet.internal.service;

import com.dotnt.booking_travel_backend.fleet.api.dtos.CreateVehicleCategoryRequest;
import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleCategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IVehicleCategoryService {
    VehicleCategoryDTO createVehicleCategory(CreateVehicleCategoryRequest request);
    Page<VehicleCategoryDTO> getVehicleCategories(Pageable pageable, String search, Boolean active);

    VehicleCategoryDTO getVehicleCategoryById(Long id);
    VehicleCategoryDTO getVehicleCategoryBySlug(String slug);

    VehicleCategoryDTO updateVehicleCategory(Long id, VehicleCategoryDTO vehicleCategoryDTO);

    void deleteVehicleCategory(Long id);
}
