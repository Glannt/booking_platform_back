package com.dotnt.booking_travel_backend.airport.api;

import com.dotnt.booking_travel_backend.airport.api.dtos.AirportTransferPriceDto;
import com.dotnt.booking_travel_backend.airport.internal.service.IAirportTransferPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/airport-transfers")
@RequiredArgsConstructor
public class AirportTransferController {
    private final IAirportTransferPriceService service;

    @PostMapping
    @PreAuthorize("hasAuthority('AIRPORT_CREATE')")
    public ResponseEntity<AirportTransferPriceDto> create(@RequestBody AirportTransferPriceDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<Page<AirportTransferPriceDto>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirportTransferPriceDto> get(@PathVariable Long id) {
        return ResponseEntity.ofNullable(service.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('AIRPORT_UPDATE')")
    public ResponseEntity<AirportTransferPriceDto> update(@PathVariable Long id, @RequestBody AirportTransferPriceDto dto) {
        return ResponseEntity.ofNullable(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('AIRPORT_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-route/{routeId}")
    public ResponseEntity<List<AirportTransferPriceDto>> getByRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(service.listByRoute(routeId));
    }
}