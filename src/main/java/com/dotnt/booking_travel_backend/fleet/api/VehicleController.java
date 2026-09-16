package com.dotnt.booking_travel_backend.fleet.api;

import com.dotnt.booking_travel_backend.fleet.api.dtos.CreateVehicleRequest;
import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleDTO;
import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleMatchingCriteria;
import com.dotnt.booking_travel_backend.fleet.internal.service.IVehicleService;
import com.dotnt.booking_travel_backend.shared.dto.ApiResponse;
import com.dotnt.booking_travel_backend.shared.i18n.I18nHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final IVehicleService vehicleService;

    @PostMapping
    @PreAuthorize("hasAuthority('VEHICLE_CREATE')")
    public ResponseEntity<VehicleDTO> create(@RequestBody CreateVehicleRequest request) {
        VehicleDTO created = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<VehicleDTO>> list(Pageable pageable,
                                                 @RequestParam(required = false) String keyword) {
        Page<VehicleDTO> page = vehicleService.getVehicles(pageable, keyword);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDTO> get(@PathVariable Long id) {
        VehicleDTO dto = vehicleService.getVehicle(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_UPDATE')")
    public ResponseEntity<VehicleDTO> update(@PathVariable Long id,
                                             @RequestBody CreateVehicleRequest request) {
        VehicleDTO updated = vehicleService.updateVehicle(id, request);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-category/{categoryId}/rentable")
    public ResponseEntity<ApiResponse<List<VehicleDTO>>> getRentableByCategory(@PathVariable Long categoryId) {
        List<VehicleDTO> vehicles = vehicleService.getRentableVehiclesByCategory(categoryId);
        String message = vehicles.isEmpty()
                ? I18nHelper.msg("vehicle.category.vehicles.empty")
                : "Operation successful";
        return ResponseEntity.ok(ApiResponse.<List<VehicleDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message(message)
                .data(vehicles)
                .build());
    }

    @GetMapping("/by-category/{categoryId}/images")
    public ResponseEntity<ApiResponse<List<com.dotnt.booking_travel_backend.media.api.MediaDto>>> getCategoryImages(@PathVariable Long categoryId) {
        List<com.dotnt.booking_travel_backend.media.api.MediaDto> images = vehicleService.getCategoryImages(categoryId);
        String message = images.isEmpty()
                ? I18nHelper.msg("vehicle.photos.empty")
                : "Operation successful";
        return ResponseEntity.ok(ApiResponse.<List<com.dotnt.booking_travel_backend.media.api.MediaDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message(message)
                .data(images)
                .build());
    }

    @GetMapping("/match")
    public ResponseEntity<List<VehicleDTO>> match(
            @RequestParam Long categoryId,
            @RequestParam(required = false) Long seatAddOnId,
            @RequestParam(required = false) Long fuelAddOnId,
            @RequestParam(required = false) List<Long> addOnIds) {
        VehicleMatchingCriteria criteria = VehicleMatchingCriteria.builder()
                .categoryId(categoryId)
                .seatAddOnId(seatAddOnId)
                .fuelAddOnId(fuelAddOnId)
                .additionalAddOnIds(addOnIds)
                .activeOnly(true)
                .canRentOnly(true)
                .build();
        return ResponseEntity.ok(vehicleService.matchVehicles(criteria));
    }
}
