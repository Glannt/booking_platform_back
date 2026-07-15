package com.dotnt.booking_travel_backend.airport.api;

import com.dotnt.booking_travel_backend.airport.api.dtos.AirportRouteDto;
import com.dotnt.booking_travel_backend.airport.internal.service.IAirportRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/airport-routes")
@RequiredArgsConstructor
public class AirportRouteController {
    private final IAirportRouteService service;

    @PostMapping
    @PreAuthorize("hasAuthority('AIRPORT_ROUTE_CREATE')")
    public ResponseEntity<AirportRouteDto> create(@RequestBody AirportRouteDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<Page<AirportRouteDto>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<List<AirportRouteDto>> listActive(@RequestParam Long airportId) {
        return ResponseEntity.ok(service.listActiveByAirport(airportId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirportRouteDto> get(@PathVariable Long id) {
        AirportRouteDto dto = service.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('AIRPORT_ROUTE_UPDATE')")
    public ResponseEntity<AirportRouteDto> update(@PathVariable Long id, @RequestBody AirportRouteDto dto) {
        AirportRouteDto updated = service.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('AIRPORT_ROUTE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

