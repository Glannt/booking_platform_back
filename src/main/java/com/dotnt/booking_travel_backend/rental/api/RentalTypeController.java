package com.dotnt.booking_travel_backend.rental.api;

import com.dotnt.booking_travel_backend.rental.api.dtos.RentalTypeDto;
import com.dotnt.booking_travel_backend.rental.internal.service.IRentalTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/rental-types")
@RequiredArgsConstructor
public class RentalTypeController {
    private final IRentalTypeService service;

    @PostMapping
    @PreAuthorize("hasAuthority('VEHICLE_CATEGORY_CREATE')")
    public ResponseEntity<RentalTypeDto> create(@RequestBody RentalTypeDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<Page<RentalTypeDto>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalTypeDto> get(@PathVariable Long id) {
        RentalTypeDto dto = service.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_CATEGORY_UPDATE')")
    public ResponseEntity<RentalTypeDto> update(@PathVariable Long id, @RequestBody RentalTypeDto dto) {
        RentalTypeDto updated = service.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_CATEGORY_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

