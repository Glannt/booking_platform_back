package com.dotnt.booking_travel_backend.airport.api;

import com.dotnt.booking_travel_backend.airport.api.dtos.AirportDto;
import com.dotnt.booking_travel_backend.airport.internal.service.IAirportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/airports")
@RequiredArgsConstructor
public class AirportController {
    private final IAirportService service;

    @PostMapping
    @PreAuthorize("hasAuthority('AIRPORT_CREATE')")
    public ResponseEntity<AirportDto> create(@RequestBody AirportDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<Page<AirportDto>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<List<AirportDto>> listActive() {
        return ResponseEntity.ok(service.listActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirportDto> get(@PathVariable Long id) {
        AirportDto dto = service.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('AIRPORT_UPDATE')")
    public ResponseEntity<AirportDto> update(@PathVariable Long id, @RequestBody AirportDto dto) {
        AirportDto updated = service.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('AIRPORT_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
