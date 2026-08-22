package com.dotnt.booking_travel_backend.fleet.api;

import com.dotnt.booking_travel_backend.fleet.api.dtos.CreateVehicleCategoryRequest;
import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleCategoryDTO;
import com.dotnt.booking_travel_backend.fleet.internal.service.IVehicleCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vehicle-categories")
@RequiredArgsConstructor
public class VehicleCategoryController {

    private final IVehicleCategoryService vehicleCategoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('VEHICLE_CATEGORY_CREATE')")
    public ResponseEntity<VehicleCategoryDTO> create(@RequestBody CreateVehicleCategoryRequest request) {
        VehicleCategoryDTO created = vehicleCategoryService.createVehicleCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<VehicleCategoryDTO>> list(Pageable pageable,
                                                         @RequestParam(required = false) String search,
                                                         @RequestParam(required = false) Boolean active) {
        Page<VehicleCategoryDTO> page = vehicleCategoryService.getVehicleCategories(pageable, search, active);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleCategoryDTO> get(@PathVariable Long id) {
        VehicleCategoryDTO dto = vehicleCategoryService.getVehicleCategoryById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<VehicleCategoryDTO> getBySlug(@PathVariable String slug) {
        VehicleCategoryDTO dto = vehicleCategoryService.getVehicleCategoryBySlug(slug);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_CATEGORY_UPDATE')")
    public ResponseEntity<VehicleCategoryDTO> update(@PathVariable Long id,
                                                     @RequestBody VehicleCategoryDTO request) {
        VehicleCategoryDTO updated = vehicleCategoryService.updateVehicleCategory(id, request);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_CATEGORY_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleCategoryService.deleteVehicleCategory(id);
        return ResponseEntity.noContent().build();
    }
}
