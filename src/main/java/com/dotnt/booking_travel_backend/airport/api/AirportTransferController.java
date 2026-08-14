package com.dotnt.booking_travel_backend.airport.api;

import com.dotnt.booking_travel_backend.airport.internal.entity.AirportTransferPrice;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportTransferPriceRepository;
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
    private final AirportTransferPriceRepository repository;

    @PostMapping
    @PreAuthorize("hasAuthority('AIRPORT_CREATE')")
    public ResponseEntity<AirportTransferPrice> create(@RequestBody AirportTransferPrice dto) {
        dto.setId(null);
        return ResponseEntity.ok(repository.save(dto));
    }

    @GetMapping
    public ResponseEntity<Page<AirportTransferPrice>> list(Pageable pageable) {
        return ResponseEntity.ok(repository.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirportTransferPrice> get(@PathVariable Long id) {
        return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('AIRPORT_UPDATE')")
    public ResponseEntity<AirportTransferPrice> update(@PathVariable Long id, @RequestBody AirportTransferPrice dto) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        dto.setId(id);
        return ResponseEntity.ok(repository.save(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('AIRPORT_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-route/{routeId}")
    public ResponseEntity<List<AirportTransferPrice>> getByRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(repository.findByAirportRouteId(routeId));
    }
}
