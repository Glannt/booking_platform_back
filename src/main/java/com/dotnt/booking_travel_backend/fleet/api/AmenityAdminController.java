package com.dotnt.booking_travel_backend.fleet.api;

import com.dotnt.booking_travel_backend.fleet.api.dtos.AmenityDTO;
import com.dotnt.booking_travel_backend.fleet.api.dtos.CreateAmenityRequest;
import com.dotnt.booking_travel_backend.fleet.internal.service.IAmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/amenities")
@RequiredArgsConstructor
public class AmenityAdminController {

    private final IAmenityService amenityService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MODERATOR')")
    public ResponseEntity<AmenityDTO> create(@RequestBody CreateAmenityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(amenityService.createAmenity(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MODERATOR')")
    public ResponseEntity<AmenityDTO> update(@PathVariable Long id, @RequestBody CreateAmenityRequest request) {
        return ResponseEntity.ok(amenityService.updateAmenity(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        amenityService.deleteAmenity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AmenityDTO> getById(@PathVariable Long id) {
        AmenityDTO dto = amenityService.getAmenityById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/by-category-id/{categoryId}")
    public ResponseEntity<List<AmenityDTO>> getByCategoryId(@PathVariable Long categoryId) {
        return ResponseEntity.ok(amenityService.getAmenitiesByCategoryId(categoryId));
    }

    @GetMapping("/by-category-code/{categoryCode}")
    public ResponseEntity<List<AmenityDTO>> getByCategoryCode(@PathVariable String categoryCode) {
        return ResponseEntity.ok(amenityService.getAmenitiesByCategoryCode(categoryCode));
    }
}
