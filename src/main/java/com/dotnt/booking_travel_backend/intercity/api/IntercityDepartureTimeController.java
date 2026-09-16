package com.dotnt.booking_travel_backend.intercity.api;

import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityDepartureTimeDto;
import com.dotnt.booking_travel_backend.intercity.internal.service.IIntercityDepartureTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/intercity-departure-times")
@RequiredArgsConstructor
public class IntercityDepartureTimeController {

    private final IIntercityDepartureTimeService departureTimeService;

    @GetMapping("/by-route/{routeId}")
    public ResponseEntity<List<IntercityDepartureTimeDto>> listByRoute(
            @PathVariable Long routeId,
            @RequestParam(required = false) String direction
    ) {
        return ResponseEntity.ok(departureTimeService.listByRoute(routeId, direction));
    }

    @GetMapping("/active/by-route/{routeId}")
    public ResponseEntity<List<IntercityDepartureTimeDto>> listActiveByRoute(
            @PathVariable Long routeId,
            @RequestParam(required = false) String direction
    ) {
        return ResponseEntity.ok(departureTimeService.listActiveByRoute(routeId, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IntercityDepartureTimeDto> get(@PathVariable Long id) {
        IntercityDepartureTimeDto dto = departureTimeService.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INTERCITY_ROUTE_UPDATE')")
    public ResponseEntity<IntercityDepartureTimeDto> create(@RequestBody IntercityDepartureTimeDto dto) {
        return ResponseEntity.ok(departureTimeService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INTERCITY_ROUTE_UPDATE')")
    public ResponseEntity<IntercityDepartureTimeDto> update(@PathVariable Long id, @RequestBody IntercityDepartureTimeDto dto) {
        IntercityDepartureTimeDto updated = departureTimeService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('INTERCITY_ROUTE_DELETE', 'INTERCITY_ROUTE_UPDATE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departureTimeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
