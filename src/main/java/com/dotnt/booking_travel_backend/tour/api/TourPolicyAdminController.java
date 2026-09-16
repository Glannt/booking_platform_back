package com.dotnt.booking_travel_backend.tour.api;

import com.dotnt.booking_travel_backend.tour.api.dtos.TourPolicyDto;
import com.dotnt.booking_travel_backend.tour.internal.service.TourPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/tour-policies")
@RequiredArgsConstructor
public class TourPolicyAdminController {

    private final TourPolicyService service;

    @GetMapping
    public ResponseEntity<Page<TourPolicyDto>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourPolicyDto> get(@PathVariable Long id) {
        TourPolicyDto dto = service.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TOUR_CREATE') or hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<TourPolicyDto> create(@RequestBody TourPolicyDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TOUR_UPDATE') or hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<TourPolicyDto> update(@PathVariable Long id, @RequestBody TourPolicyDto dto) {
        TourPolicyDto updated = service.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('TOUR_UPDATE') or hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<TourPolicyDto> toggleActive(@PathVariable Long id) {
        TourPolicyDto updated = service.toggleActive(id);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TOUR_DELETE') or hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
