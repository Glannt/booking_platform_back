package com.dotnt.booking_travel_backend.fleet.internal.service;

import com.dotnt.booking_travel_backend.fleet.api.dtos.CreateVehicleRequest;
import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IVehicleService {
    VehicleDTO createVehicle(CreateVehicleRequest request);
    Page<VehicleDTO> getVehicles(Pageable pageable, String keyword);
    VehicleDTO getVehicle(Long id);
    VehicleDTO updateVehicle(Long id, CreateVehicleRequest request);
    void deleteVehicle(Long id);
    List<VehicleDTO> getRentableVehiclesByCategory(Long categoryId);
}
