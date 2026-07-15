package com.dotnt.booking_travel_backend.fleet.api;

import com.dotnt.booking_travel_backend.fleet.api.dtos.CreateVehicleRequest;
import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleDTO;
import com.dotnt.booking_travel_backend.fleet.internal.service.IVehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final IVehicleService vehicleService;

    @PostMapping
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
    public ResponseEntity<VehicleDTO> update(@PathVariable Long id,
                                             @RequestBody CreateVehicleRequest request) {
        VehicleDTO updated = vehicleService.updateVehicle(id, request);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}

