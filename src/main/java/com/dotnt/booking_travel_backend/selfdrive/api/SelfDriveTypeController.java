package com.dotnt.booking_travel_backend.selfdrive.api;

import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDriveTypeDto;
import com.dotnt.booking_travel_backend.selfdrive.internal.service.ISelfDriveTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/admin/self-drive-types", "/api/v1/admin/rental-types"})
@RequiredArgsConstructor
public class SelfDriveTypeController {
    private final ISelfDriveTypeService typeService;

    @PostMapping
    @PreAuthorize("hasAuthority('VEHICLE_CREATE')")
    public ResponseEntity<SelfDriveTypeDto> create(@RequestBody SelfDriveTypeDto dto) {
        return ResponseEntity.ok(typeService.create(dto));
    }

    @GetMapping
    public ResponseEntity<Page<SelfDriveTypeDto>> list(Pageable pageable) {
        return ResponseEntity.ok(typeService.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SelfDriveTypeDto> get(@PathVariable Long id) {
        SelfDriveTypeDto dto = typeService.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_UPDATE')")
    public ResponseEntity<SelfDriveTypeDto> update(@PathVariable Long id, @RequestBody SelfDriveTypeDto dto) {
        SelfDriveTypeDto updated = typeService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        typeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
