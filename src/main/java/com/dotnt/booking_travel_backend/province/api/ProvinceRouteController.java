package com.dotnt.booking_travel_backend.province.api;

import com.dotnt.booking_travel_backend.province.api.dtos.ProvinceRouteDto;
import com.dotnt.booking_travel_backend.province.internal.service.IProvinceRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/province-routes")
@RequiredArgsConstructor
public class ProvinceRouteController {
    private final IProvinceRouteService service;

    @PostMapping
    @PreAuthorize("hasAuthority('INTERCITY_ROUTE_CREATE')")
    public ResponseEntity<ProvinceRouteDto> create(@RequestBody ProvinceRouteDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<Page<ProvinceRouteDto>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProvinceRouteDto> get(@PathVariable Long id) {
        ProvinceRouteDto dto = service.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INTERCITY_ROUTE_UPDATE')")
    public ResponseEntity<ProvinceRouteDto> update(@PathVariable Long id, @RequestBody ProvinceRouteDto dto) {
        ProvinceRouteDto updated = service.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INTERCITY_ROUTE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

