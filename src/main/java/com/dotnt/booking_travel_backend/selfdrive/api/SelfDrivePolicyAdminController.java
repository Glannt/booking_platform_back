package com.dotnt.booking_travel_backend.selfdrive.api;

import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDrivePolicyDto;
import com.dotnt.booking_travel_backend.selfdrive.internal.service.ISelfDrivePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/self-drive-policies")
@RequiredArgsConstructor
public class SelfDrivePolicyAdminController {

    private final ISelfDrivePolicyService service;

    @GetMapping
    public ResponseEntity<Page<SelfDrivePolicyDto>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SelfDrivePolicyDto> get(@PathVariable Long id) {
        SelfDrivePolicyDto dto = service.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VEHICLE_CREATE') or hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<SelfDrivePolicyDto> create(@RequestBody SelfDrivePolicyDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_UPDATE') or hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<SelfDrivePolicyDto> update(@PathVariable Long id, @RequestBody SelfDrivePolicyDto dto) {
        SelfDrivePolicyDto updated = service.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('VEHICLE_UPDATE') or hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<SelfDrivePolicyDto> toggleActive(@PathVariable Long id) {
        SelfDrivePolicyDto updated = service.toggleActive(id);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_DELETE') or hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
